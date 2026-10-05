package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockBooleanDirectrix;
import com.mojang.datafixers.util.Pair;
import kotlin.jvm.optionals.OptionalsKt;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.StorageLociEntity;
import org.sophia.slate_work.blocks.entities.TradeLociEntity;
import org.sophia.slate_work.casting.mishap.MishapNoStorageLoci;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleInvalidIota;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleNotEnoughArgs;
import org.sophia.slate_work.registries.SlateWorksBlockRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.sophia.slate_work.misc.CircleHelper.*;

@SuppressWarnings({"UnstableApiUsage"})
public class TradeLoci extends BlockBooleanDirectrix implements BlockEntityProvider {
    public TradeLoci(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TradeLociEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        //return world.isClient ? null : BeehiveBlock.checkType(type, BlockRegistry.TRADE_LOCI_ENTITY, TradeLociEntity::serverTick);
        return (!world.isClient || SlateWorksBlockRegistry.TRADE_LOCI_ENTITY != type) ? TradeLociEntity::tick : null;
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        List<Pair<BlockPos, Direction>> exit = new ArrayList<>();
        if (world.getBlockEntity(pos) instanceof TradeLociEntity entity) {
            var stack = imageIn.getStack();

            if (stack.isEmpty()) {
                var list = world.getEntitiesByClass(VillagerEntity.class, (new Box(pos)).expand(10), (a) -> true);
                if (!list.isEmpty()) {
                    entity.slurpVillager(list.get(0));
                }

                this.fakeThrowMishap(
                        pos, bs, imageIn, env,
                        new MishapSpellCircleNotEnoughArgs(1,0, pos)
                );
                return new ControlFlow.Stop();
            }

            var last = stack.last();
            if (!(last instanceof DoubleIota)) {
                this.fakeThrowMishap(
                        pos, bs, imageIn, env,
                        MishapSpellCircleInvalidIota.ofType(last, 0, "double", pos)
                );
                return new ControlFlow.Stop();
            }

            int index = (int) Math.round(((DoubleIota) last).getDouble());
            if (index > entity.offerList.size()-1 || index < 0) {
                this.fakeThrowMishap(
                        pos, bs, imageIn, env,
                        MishapSpellCircleInvalidIota.of(last, 0, "double.between", pos,entity.offerList.size()-1, 0)
                );
                return new ControlFlow.Stop();
            }
            var storages = INSTANCE.getLists(env);
            if (storages.isEmpty()) {
                this.fakeThrowMishap(
                        pos, bs, imageIn, env,
                        new MishapNoStorageLoci(pos)
                );
                return new ControlFlow.Stop();
            }
            // All the checks are done, now for the more checks
            TradeOffer offer = entity.offerList.get(index);

            ItemStack firstBuyItem = offer.getFirstBuyItem().itemStack();
            ItemStack secondBuyItem = OptionalsKt.getOrNull(offer.getSecondBuyItem().map(TradedItem::itemStack));

            ItemSlot firstItem = storages.get(ItemVariant.of(firstBuyItem));
            ItemSlot secondItem = null;
            if (secondBuyItem != null) {
                secondItem = storages.get(ItemVariant.of(secondBuyItem));
            }
            if (firstItem == null || secondItem == null ||
                    firstItem.getCount() < firstBuyItem.getCount() ||
                    secondItem.getCount() < secondBuyItem.getCount() ||
                    offer.isDisabled()
            ) {
                exit.add(this.exitPositionFromDirection(pos, bs.get(FACING).getOpposite()));
                world.setBlockState(pos, bs.with(STATE, State.FALSE));
                return new ControlFlow.Continue(imageIn, exit);
            }

            // So everything can now be traded
            int xp = offer.getMerchantExperience();
            // God this is... interesting

            try (Transaction transaction = Transaction.openOuter()){
                StorageLociEntity loci1 = (StorageLociEntity) env.getWorld().getBlockEntity(firstItem.getPos());
                StorageLociEntity loci2 = (StorageLociEntity) env.getWorld().getBlockEntity(secondItem.getPos());

                long firstItemExtracted = loci1.extract(firstItem.getItem(), firstBuyItem.getCount(), transaction);
                long secondItemExtracted = loci2.extract(secondItem.getItem(), secondBuyItem.getCount(), transaction);

                if (firstItemExtracted == firstBuyItem.getCount() && secondItemExtracted == secondBuyItem.getCount()) {
                    transaction.commit();

                    offer.use();
                    INSTANCE.storeItems(env, offer.copySellItem());
                    entity.xp += xp;

                }
            }

            // Wrap it up folks!
            entity.levelUpCheck();
            entity.markDirty();
            exit.add(this.exitPositionFromDirection(pos, bs.get(FACING)));
            world.setBlockState(pos, bs.with(STATE, State.TRUE));
            return new ControlFlow.Continue(imageIn, exit);
        }

        return new ControlFlow.Stop();
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof TradeLociEntity && !newState.isOf(state.getBlock())) {
            if (!world.isClient) {
                ItemStack itemStack = new ItemStack(SlateWorksBlockRegistry.TRADE_LOCI);
                blockEntity.setStackNbt(itemStack, world.getRegistryManager());
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockState endEnergized(BlockPos pos, BlockState bs, World world) {
        return super.endEnergized(pos, bs, world).with(STATE, State.NEITHER);
    }
}
