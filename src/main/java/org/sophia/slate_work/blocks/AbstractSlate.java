package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.ExecutionClientView;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.stream.Stream;

import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.*;

// We needed a Slate Block without the Attached_Face prop. So that's why this ugly ass file exists
public abstract class AbstractSlate extends BlockCircleComponent implements BlockEntityProvider {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final DirectionProperty FACING = Properties.FACING;


    public AbstractSlate(Settings p_49795_) {
        super(p_49795_);
        this.setDefaultState(this.stateManager.getDefaultState().with(ENERGIZED, false).with(FACING, Direction.NORTH).with(WATERLOGGED, false));
    }

    public boolean isTransparent(BlockState state, BlockView reader, BlockPos pos) {
        return !(Boolean)state.get(WATERLOGGED);
    }

    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    public ICircleComponent.ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        BlockEntity var9 = world.getBlockEntity(pos);
        if (var9 instanceof BlockEntitySlate tile) {
            HexPattern pattern = tile.pattern;
            EnumSet<Direction> exitDirsSet = this.possibleExitDirections(pos, bs, world);
            exitDirsSet.remove(enterDir.getOpposite());
            Stream exitDirs = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(pos, dir));
            if (pattern == null) {
                return new ICircleComponent.ControlFlow.Continue(imageIn, exitDirs.toList());
            } else {
                CastingVM vm = new CastingVM(imageIn, env);
                ExecutionClientView result = vm.queueExecuteAndWrapIota(new PatternIota(pattern), world);
                return (result.getResolutionType().getSuccess() ? new ICircleComponent.ControlFlow.Continue(vm.getImage(), exitDirs.toList()) : new ICircleComponent.ControlFlow.Stop());
            }
        } else {
            return new ICircleComponent.ControlFlow.Stop();
        }
    }

    public boolean canEnterFromDirection(Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        Direction thisNormal = this.normalDir(pos, bs, world);
        return enterDir != thisNormal.getOpposite();
    }

    public EnumSet<Direction> possibleExitDirections(BlockPos pos, BlockState bs, World world) {
        EnumSet<Direction> allDirs = EnumSet.allOf(Direction.class);
        Direction normal = this.normalDir(pos, bs, world);
        allDirs.remove(normal);
        return allDirs;
    }

    public Direction normalDir(BlockPos pos, BlockState bs, World world, int recursionLeft) {
        return bs.get(FACING);
    }

    public float particleHeight(BlockPos pos, BlockState bs, World world) {
        return -0.4375F;
    }

    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return switch (pState.get(FACING)) {
            case DOWN -> AABB_FLOOR;
            case UP -> AABB_CEILING;
            case NORTH -> AABB_NORTH_WALL;
            case SOUTH -> AABB_SOUTH_WALL;
            case WEST -> AABB_WEST_WALL;
            case EAST -> AABB_EAST_WALL;
        };
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, WATERLOGGED);
    }

    public @Nullable BlockState getPlacementState(ItemPlacementContext pContext) {
        FluidState fluidState = pContext.getWorld().getFluidState(pContext.getBlockPos());

        for(Direction direction : pContext.getPlacementDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = (this.getDefaultState()).with(FACING, pContext.getVerticalPlayerLookDirection().getOpposite());
            } else {
                blockstate = this.getDefaultState().with(FACING, direction.getOpposite());
            }

            blockstate = blockstate.with(WATERLOGGED, fluidState.isIn(FluidTags.WATER) && fluidState.getLevel() == 8);
            if (blockstate.canPlaceAt(pContext.getWorld(), pContext.getBlockPos())) {
                return blockstate;
            }
        }

        return null;
    }

    public boolean canPlaceAt(BlockState pState, WorldView pLevel, BlockPos pPos) {
        return canAttach(pLevel, pPos, getConnectedDirection(pState).getOpposite());
    }

    public BlockState getStateForNeighborUpdate(BlockState pState, Direction pFacing, BlockState pFacingState, WorldAccess pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.get(WATERLOGGED)) {
            pLevel.scheduleFluidTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickRate(pLevel));
        }

        return getConnectedDirection(pState).getOpposite() == pFacing && !pState.canPlaceAt(pLevel, pCurrentPos) ? pState.getFluidState().getBlockState() : super.getStateForNeighborUpdate(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public static boolean canAttach(WorldView pReader, BlockPos pPos, Direction pDirection) {
        BlockPos blockpos = pPos.offset(pDirection);
        return pReader.getBlockState(blockpos).isSideSolidFullSquare(pReader, blockpos, pDirection.getOpposite());
    }

    protected static Direction getConnectedDirection(BlockState pState) {
        return pState.get(FACING);
    }

    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}
