package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.SaveLociEntity;

import java.util.EnumSet;

import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.AABB_FLOOR;
import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.FACING;

public class SaveLoci extends BlockCircleComponent implements BlockEntityProvider {
    public static final BooleanProperty TOP_PART = Properties.UP;
    public static final DirectionProperty HORIZONTAL = Properties.HORIZONTAL_FACING;

    public SaveLoci(Settings p_49795_) {
        super(p_49795_);
        this.setDefaultState(this.stateManager.getDefaultState().with(ENERGIZED, false).with(TOP_PART, false).with(HORIZONTAL, Direction.NORTH));
    }

    @Override
    public Direction normalDir(BlockPos blockPos, BlockState blockState, World world, int i) {
        return Direction.UP;
    }

    @Override
    public float particleHeight(BlockPos blockPos, BlockState blockState, World world) {
        return 0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TOP_PART, FACING);
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage castingImage, CircleCastEnv circleCastEnv, Direction direction, BlockPos blockPos, BlockState blockState, ServerWorld serverWorld) {
        BlockPos entityPos = blockPos;
        if (blockState.get(TOP_PART)) entityPos = blockPos.down();
        if (serverWorld.getBlockEntity(entityPos) instanceof SaveLociEntity entity) {
            var newCastingImage = entity.swapSave(castingImage, serverWorld);

            var exitDirsSet = this.possibleExitDirections(blockPos, blockState, serverWorld);
            exitDirsSet.remove(direction.getOpposite());
            var exits = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(blockPos, dir)).toList();
            return new ControlFlow.Continue(newCastingImage, exits);
        }
        return new ControlFlow.Stop();
    }

    @Override
    public boolean canEnterFromDirection(Direction direction, BlockPos blockPos, BlockState blockState, ServerWorld serverWorld) {
        return direction != Direction.DOWN;
    }

    @Override
    public EnumSet<Direction> possibleExitDirections(BlockPos blockPos, BlockState blockState, World world) {
        EnumSet<Direction> z = EnumSet.allOf(Direction.class);
        z.remove(Direction.UP);
        return z;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        int offset = 0;
        if (state.get(TOP_PART)) offset = -1;
        return VoxelShapes.union(AABB_FLOOR,
                createCuboidShape(1,1,1,15,31,15)).offset(0,offset,0);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            BlockPos blockPos = pos.up();
            world.setBlockState(blockPos, state.with(TOP_PART, true), Block.NOTIFY_ALL);
            world.updateNeighbors(pos, Blocks.AIR);
            state.updateNeighbors(world, pos, Block.NOTIFY_ALL);
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.isAir(pos.up());
    }

    @Override
    public BlockState rotate(BlockState pState, BlockRotation pRot) {
        return pState.with(FACING, pRot.rotate(pState.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, BlockMirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.get(FACING)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        var dir = pContext.getHorizontalPlayerFacing();
        if (pContext.getPlayer() != null && !pContext.getPlayer().isSneaky()) {
            dir = dir.getOpposite();
        }
        return this.getDefaultState().with(HORIZONTAL, dir);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (!state.isOf(newState.getBlock()) || state.get(FACING) != newState.get(FACING)){
            if (!world.isClient) {
                BlockPos blockPos;
                if (state.get(TOP_PART)) blockPos = pos.down();
                    else blockPos = pos.up();

                BlockState blockState = world.getBlockState(blockPos);
                if (blockState.isOf(this)) {
                    world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
                    world.syncWorldEvent(null, WorldEvents.BLOCK_BROKEN, blockPos, Block.getRawIdFromState(blockState));
                }
            }
        }
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (!state.get(TOP_PART))
            return new SaveLociEntity(pos, state);
        return null;
    }
}

