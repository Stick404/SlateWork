package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleInvalidIota;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleNotEnoughArgs;

import java.util.ArrayList;

public class MuteLoci extends AbstractSlate {
    public static final DirectionProperty FACING = Properties.FACING;

    public MuteLoci(Settings p_49795_) {
        super(p_49795_);
        this.setDefaultState(this.stateManager.getDefaultState().with(ENERGIZED, false).with(FACING, Direction.NORTH).with(WATERLOGGED, false));
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage castingImage, CircleCastEnv circleCastEnv, Direction direction,
                                         BlockPos blockPos, BlockState blockState, ServerWorld serverWorld) {
        ArrayList<Iota> stack = new ArrayList<>(castingImage.getStack());
        var data = castingImage.getUserData().copy();

        var exitDirsSet = this.possibleExitDirections(blockPos, blockState, serverWorld);
        exitDirsSet.remove(direction.getOpposite());
        var exits = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(blockPos, dir)).toList();

        if (stack.isEmpty()) {
            this.fakeThrowMishap(
                    blockPos, blockState, castingImage, circleCastEnv,
                    new MishapSpellCircleNotEnoughArgs(1,0,blockPos)
            );
            return new ControlFlow.Stop();
        }

        var last = stack.get(stack.size() -1);
        stack.remove(stack.size() -1);
        if (!(last instanceof DoubleIota)) {
            this.fakeThrowMishap(
                    blockPos, blockState, castingImage, circleCastEnv,
                    MishapSpellCircleInvalidIota.ofType(last, 0, "double", blockPos)
            );
            return new ControlFlow.Stop();
        }

        var volume = ((DoubleIota) last).getDouble();
        if (volume > 1 || volume < 0) {
            this.fakeThrowMishap(
                    blockPos, blockState, castingImage, circleCastEnv,
                    MishapSpellCircleInvalidIota.of(last, 0, "double.between", blockPos,1, 0)
            );
            return new ControlFlow.Stop();
        }

        // Everything *should* be valid now

        if (volume == 1){
            data.putBoolean("mute", false);
        } else {
            data.putBoolean("mute", true);
            data.putFloat("volume", (float) volume);
        }
        return new ControlFlow.Continue(
                castingImage.copy(stack, castingImage.getParenCount(), castingImage.getParenthesized(),
                        castingImage.getEscapeNext(), castingImage.getOpsConsumed(), data), exits);
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)){
            case DOWN  -> createCuboidShape(0.0, 16 -3.0, 0.0, 16.0, 16.0, 16.0);
            case UP -> createCuboidShape(0.0,0.0,0.0,16.0,3.0,16.0);
            case NORTH -> createCuboidShape(0.0, 0.0, 16 -3.0, 16.0, 16.0, 16.0);
            case SOUTH -> createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
            case WEST -> createCuboidShape(16 -3.0, 0.0, 0.0, 16.0, 16.0, 16.0);
            case EAST -> createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
        };
    }
}
