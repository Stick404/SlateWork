package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockBooleanDirectrix;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeehiveBlockEntity;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.StorageLociEntity;
import org.sophia.slate_work.blocks.entities.TradeLociEntity;
import org.sophia.slate_work.casting.mishap.MishapNoStorageLoci;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleInvalidIota;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleNotEnoughArgs;
import org.sophia.slate_work.registries.BlockRegistry;

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
        return (!world.isClient || BlockRegistry.TRADE_LOCI_ENTITY != type) ? TradeLociEntity::tick : null;
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        List<Pair<BlockPos, Direction>> exit = new ArrayList<>();
        if (world.getBlockEntity(pos) instanceof TradeLociEntity entity) {
            ArrayList<Iota> stack = new ArrayList<>(imageIn.getStack());

            if (stack.isEmpty()) {
                var list = world.getEntitiesByClass(VillagerEntity.class, (new Box(pos, pos)).expand(10), (a) -> true);
                if (!list.isEmpty()) {
                    entity.slurpVillager(list.get(0));
                }

                this.fakeThrowMishap(
                        pos, bs, imageIn, env,
                        new MishapSpellCircleNotEnoughArgs(1,0, pos)
                );
                return new ControlFlow.Stop();
            }

            var last = stack.get(stack.size() -1);
            stack.remove(stack.size() -1);
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

            ItemStack firstBuyItem = offer.getAdjustedFirstBuyItem();
            ItemStack secondBuyItem = offer.getSecondBuyItem();

            ItemSlot firstItem = storages.get(ItemVariant.of(firstBuyItem));
            ItemSlot secondItem = storages.get(ItemVariant.of(secondBuyItem));
            if (firstItem == null || secondItem == null ||
                    firstItem.component2() < firstBuyItem.getCount() ||
                    secondItem.component2() < secondBuyItem.getCount() ||
                    offer.isDisabled()
            ) {
                exit.add(this.exitPositionFromDirection(pos, bs.get(FACING).getOpposite()));
                return new ControlFlow.Continue(imageIn, exit);
            }
            // So everything can now be traded
            int xp = offer.getMerchantExperience();
            // God this is... interesting
            firstItem.getStorageLociEntity().setStack(firstItem.getStorageLociEntity().getSlot(firstItem.getItem()),
                    new net.minecraft.util.Pair<>(firstItem.getItem(), firstItem.getCount() -firstBuyItem.getCount()));
            secondItem.getStorageLociEntity().setStack(secondItem.getStorageLociEntity().getSlot(secondItem.getItem()),
                    new net.minecraft.util.Pair<>(secondItem.getItem(), secondItem.getCount() -firstBuyItem.getCount()));
            offer.use();
            INSTANCE.storeItems(env, offer.copySellItem());
            entity.xp += xp;

            // Wrap it up folks!
            entity.levelUpCheck();
            exit.add(this.exitPositionFromDirection(pos, bs.get(FACING)));
            return new ControlFlow.Continue(imageIn, exit);
        }

        return new ControlFlow.Stop();
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof TradeLociEntity && !newState.isOf(state.getBlock())) {
            if (!world.isClient) {
                ItemStack itemStack = new ItemStack(BlockRegistry.TRADE_LOCI);
                blockEntity.setStackNbt(itemStack);
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
