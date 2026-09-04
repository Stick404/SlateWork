package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.*;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.BlockBreakLociEntity;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleNotEnoughArgs;
import org.sophia.slate_work.registries.BlockRegistry;
import org.sophia.slate_work.registries.PatternRegistry;

import java.util.ArrayList;

public class BlockBreakLoci extends AbstractSlate implements BlockEntityProvider {
    public static final int DISTANCE = 6;
    private static final double THICKNESS = 4;
    private static final VoxelShape DOWN_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, 16, THICKNESS, 16),
            Block.createCuboidShape(3,3,3,13,9,13));
    private static final VoxelShape UP_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 16 - THICKNESS, 0, 16, 16, 16),
            Block.createCuboidShape(3,7,3,13,13,13));
    private static final VoxelShape EAST_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, THICKNESS, 16, 16),
            Block.createCuboidShape(7,3,3,9,13,13));
    private static final VoxelShape WEST_AB = VoxelShapes.union(
            Block.createCuboidShape(16 - THICKNESS, 0, 0, 16, 16, 16),
            Block.createCuboidShape(7,3,3,13,13,13));
    private static final VoxelShape NORTH_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 16 - THICKNESS, 16, 16, 16),
            Block.createCuboidShape(3,3,7,13,13,13));
    private static final VoxelShape SOUTH_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, 16, 16, THICKNESS),
            Block.createCuboidShape(3,3,3,13,13,9));


    public BlockBreakLoci(Settings p_49795_) {
        super(p_49795_);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return switch (pState.get(FACING)){
            case NORTH -> NORTH_AB;
            case SOUTH -> SOUTH_AB;
            case WEST -> WEST_AB;
            case EAST -> EAST_AB;
            case UP -> DOWN_AB;
            case DOWN -> UP_AB;
        };
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        // Get the stack/exit dirs
        ArrayList<Iota> stack = new ArrayList<>(imageIn.getStack());
        var exitDirsSet = this.possibleExitDirections(pos, bs, world);
        exitDirsSet.remove(enterDir.getOpposite());
        var exits = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(pos, dir)).toList();

        var hex = stack.get(stack.size() -1);
        if (stack.isEmpty()) {
            this.fakeThrowMishap(
                    pos, bs, imageIn, env,
                    new MishapSpellCircleNotEnoughArgs(1,0, pos)
            );
            return new ControlFlow.Stop();
        }

        var vm = new CastingVM(imageIn, env);
        var result = vm.queueExecuteAndWrapIota(new PatternIota(PatternRegistry.I_AM_SO_SORRY_FOR_MY_CRIMES_COMMA_HEXXY_FORGIVE_ME), world);

        if (result.getResolutionType().getSuccess()) {
            // We play the sound at the locus to not explode player's ears
            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.25f, 0.6f);
            world.getPlayers().forEach(a -> a.networkHandler.sendPacket(new ParticleS2CPacket(
                    ParticleTypes.EXPLOSION, false, pos.toCenterPos().getX(), pos.toCenterPos().getY(),pos.toCenterPos().getZ(),
                    0, 0, 0, 0, 1
            )));

            var image = vm.getImage();
            var newestStack = new ArrayList<>(image.getStack());
            newestStack.add(hex);

            return new ControlFlow.Continue(image.copy(newestStack, image.getParenCount(),
                    image.getParenthesized(),image.getEscapeNext(),image.getOpsConsumed(), image.getUserData()), exits);
        } else {
            return new ControlFlow.Stop();
        }
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