package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.BroadcasterLociEntity;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleInvalidIota;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleNotEnoughArgs;
import org.sophia.slate_work.misc.KnownBroadcasters;

import java.util.ArrayList;
import java.util.EnumSet;

public class BroadcasterLoci extends BlockCircleComponent implements BlockEntityProvider {
    public BroadcasterLoci(Settings p_49795_) {
        super(p_49795_);
        this.setDefaultState(this.stateManager.getDefaultState().with(ENERGIZED, false));
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
    public ControlFlow acceptControlFlow(CastingImage castingImage, CircleCastEnv circleCastEnv, Direction direction, BlockPos blockPos, BlockState blockState, ServerWorld serverWorld) {
        if (serverWorld.getBlockEntity(blockPos) instanceof BroadcasterLociEntity entity){
            var stack = new ArrayList<>(castingImage.getStack());

            if (stack.isEmpty()) { // Feels silly, but this is what Hex does
                this.fakeThrowMishap(
                        blockPos, blockState, castingImage, circleCastEnv,
                        new MishapSpellCircleNotEnoughArgs(1,0, blockPos));
                return new ControlFlow.Stop();
            }
            int index = stack.size() -1;
            var iota = stack.get(index);
            stack.remove(index);

            if (iota.size() > 1){ // Don't want to store "complex" iotas, like lists and Jumps
                this.fakeThrowMishap(blockPos, blockState, castingImage, circleCastEnv,
                        MishapSpellCircleInvalidIota.of(iota, 0,"simpler_iota", blockPos));
                return new ControlFlow.Stop();
            }
            var truename = MishapOthersName.getTrueNameFromDatum(iota, null);
            if (truename != null){
                this.fakeThrowMishap(blockPos, blockState, castingImage, circleCastEnv,
                        new MishapOthersName(truename));
                return new ControlFlow.Stop();
            }

            entity.setIota(iota);

            var exitDirsSet = this.possibleExitDirections(blockPos, blockState, serverWorld);
            exitDirsSet.remove(direction.getOpposite());
            var exits = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(blockPos, dir)).toList();

            return new ControlFlow.Continue(castingImage.copy(
                    stack, castingImage.getParenCount(), castingImage.getParenthesized(), castingImage.getEscapeNext(), castingImage.getOpsConsumed(), castingImage.getUserData()
            ), exits);
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
        return VoxelShapes.union(
                createCuboidShape(0,0,0,16,3,16),
                createCuboidShape(0,3,0,4,9,4),
                createCuboidShape(12,3,0,16,9,4),
                createCuboidShape(0,3,12,4,9,16),
                createCuboidShape(12,3,12,16,9,16),
                createCuboidShape(0,9,0,16,12,16),
                createCuboidShape(7,0,7,9,9,9)
        );
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
         if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof BroadcasterLociEntity broadcaster){
            KnownBroadcasters.INSTANCE.removeBroadcaster(broadcaster);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BroadcasterLociEntity(pos, state);
    }
}
