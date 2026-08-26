package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.BlockBreakLociEntity;
import org.sophia.slate_work.blocks.entities.StorageLociEntity;
import org.sophia.slate_work.misc.CircleHelper;
import org.sophia.slate_work.registries.BlockRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockBreakLoci extends AbstractSlate implements BlockEntityProvider {
    public BlockBreakLoci(Settings p_49795_) {
        super(p_49795_);
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        ArrayList<Iota> stack = new ArrayList<>(imageIn.getStack());

        BlockEntity entity = world.getBlockEntity(pos);
        if (!(entity instanceof BlockBreakLociEntity loci)){
            return new ICircleComponent.ControlFlow.Stop();
        }

        ItemStack fakePick = new ItemStack(Items.NETHERITE_PICKAXE);
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.fromNbt(loci.getEnchantments());

        // TODO: Costs

        EnchantmentHelper.set(enchantments, fakePick);

        Direction facing = bs.get(FACING);
        BlockPos targetPos = pos.add(facing.getVector());
        BlockState targetBlock = world.getBlockState(targetPos);
        BlockEntity targetEntity = world.getBlockEntity(targetPos);
        Vec3d center = targetPos.toCenterPos();

        List<ItemStack> droppedItems = Block.getDroppedStacks(targetBlock, world, pos, targetEntity, null, fakePick);
        world.breakBlock(targetPos, false);

        for (ItemStack item : droppedItems){
            if (!CircleHelper.INSTANCE.storeItems(env, item)){
                // If it fails to store, spit item out
                ItemEntity itemEntity = new ItemEntity(world, center.getX(), center.getY(), center.getZ(), item);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }

        var exitDirsSet = this.possibleExitDirections(pos, bs, world);
        exitDirsSet.remove(enterDir.getOpposite());
        var exits = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(pos, dir)).toList();
        return new ICircleComponent.ControlFlow.Continue(imageIn.copy(stack, imageIn.getParenCount(),
                imageIn.getParenthesized(), imageIn.getEscapeNext(), imageIn.getOpsConsumed(), imageIn.getUserData()), exits);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlockBreakLociEntity(pos, state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.getBlockEntity(pos) instanceof BlockBreakLociEntity loci && !world.isClient){
            loci.setEnchantments(itemStack.getEnchantments());
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BlockBreakLociEntity loci && !newState.isOf(state.getBlock())) {
            if (!world.isClient) {
                ItemStack itemStack = new ItemStack(BlockRegistry.BLOCK_BREAKING_LOCI_ITEM);

                var enchants = EnchantmentHelper.fromNbt(loci.getEnchantments());
                for (var enchant : enchants.entrySet()){
                    itemStack.addEnchantment(enchant.getKey(), enchant.getValue());
                }

                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
