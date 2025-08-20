package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.HotbarLociEntity;

import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.*;
import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.AABB_NORTH_WALL;
import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.AABB_SOUTH_WALL;
import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.AABB_WEST_WALL;

public class HotbarLoci extends AbstractSlate implements BlockEntityProvider {
    public HotbarLoci(Settings p_49795_) {
        super(p_49795_);
    }

    private static final VoxelShape DOWN_AB = VoxelShapes.union(AABB_FLOOR,
            BlockSlate.createCuboidShape(1,1,1,15,4,15));
    private static final VoxelShape UP_AB = VoxelShapes.union(AABB_CEILING,
            BlockSlate.createCuboidShape(1,12,1,15,15,15));
    private static final VoxelShape EAST_AB = VoxelShapes.union(AABB_EAST_WALL,
            BlockSlate.createCuboidShape(1,1,1,4,15,15));
    private static final VoxelShape WEST_AB = VoxelShapes.union(AABB_WEST_WALL,
            BlockSlate.createCuboidShape(12,1,1,15,15,15));
    private static final VoxelShape NORTH_AB = VoxelShapes.union(AABB_NORTH_WALL,
            BlockSlate.createCuboidShape(1,1,12,15,15,15));
    private static final VoxelShape SOUTH_AB = VoxelShapes.union(AABB_SOUTH_WALL,
            BlockSlate.createCuboidShape(1,1,1,15,15,4));

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        if (world.getBlockEntity(pos) instanceof HotbarLociEntity){
            var data =imageIn.getUserData().copy();
            data.put("hotbar_loci", NbtHelper.fromBlockPos(pos));
            var exitDirsSet = this.possibleExitDirections(pos, bs, world);
            exitDirsSet.remove(enterDir.getOpposite());
            var exitDirs = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(pos, dir));

            return new ControlFlow.Continue(
                    imageIn.copy(imageIn.getStack(), imageIn.getParenCount(), imageIn.getParenthesized(),
                            imageIn.getEscapeNext(), imageIn.getOpsConsumed(), data), exitDirs.toList());
        }
        return new ControlFlow.Stop();
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HotbarLociEntity(pos, state);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)){
            case NORTH -> NORTH_AB;
            case SOUTH -> SOUTH_AB;
            case WEST -> WEST_AB;
            case EAST -> EAST_AB;
            case UP -> DOWN_AB;
            case DOWN -> UP_AB;
        };
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!newState.isOf(state.getBlock())) {
            if (world instanceof ServerWorld sWorld && sWorld.getBlockEntity(pos) instanceof HotbarLociEntity entity) {
                ItemScatterer.spawn(world, pos, entity);
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof HotbarLociEntity loci) {
                player.openHandledScreen(loci);
            }
            return ActionResult.CONSUME;
        }
    }
}
