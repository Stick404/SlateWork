package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.AABB_FLOOR;

public class SaveLoci extends BlockCircleComponent {
    public static final BooleanProperty TOP_PART = Properties.UP;

    public SaveLoci(Settings p_49795_) {
        super(p_49795_);
        this.setDefaultState(this.stateManager.getDefaultState().with(ENERGIZED, false).with(TOP_PART, false));
    }

    @Override
    public Direction normalDir(BlockPos blockPos, BlockState blockState, World world, int i) {
        return null;
    }

    @Override
    public float particleHeight(BlockPos blockPos, BlockState blockState, World world) {
        return 0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TOP_PART);
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage castingImage, CircleCastEnv circleCastEnv, Direction direction, BlockPos blockPos, BlockState blockState, ServerWorld serverWorld) {
        return null;
    }

    @Override
    public boolean canEnterFromDirection(Direction direction, BlockPos blockPos, BlockState blockState, ServerWorld serverWorld) {
        return false;
    }

    @Override
    public EnumSet<Direction> possibleExitDirections(BlockPos blockPos, BlockState blockState, World world) {
        return null;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        //TODO: Give this a better custom shape
        if (state.get(TOP_PART)) return VoxelShapes.empty();
        return VoxelShapes.union(AABB_FLOOR,
                createCuboidShape(1,1,1,15,31,15));
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
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && player.isCreative()) {
            if (state.get(TOP_PART)) {
                BlockPos blockPos = pos.down();
                BlockState blockState = world.getBlockState(blockPos);
                if (blockState.isOf(this) && !blockState.get(TOP_PART)) {
                    world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
                    world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, blockPos, Block.getRawIdFromState(blockState));
                }
            }
        }
        super.onBreak(world, pos, state, player);
    }
}

