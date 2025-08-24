package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleInvalidIota;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleNotEnoughArgs;
import org.sophia.slate_work.misc.ICircleSpeedValue;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class RedstoneLoci extends AbstractSlate {
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final IntProperty POWER = Properties.POWER;

    @Override
    public Direction normalDir(BlockPos pos, BlockState bs, World world, int recursionLeft) {
        return super.normalDir(pos, bs, world, recursionLeft);
    }

    public RedstoneLoci(Settings p_49795_) {
        super(p_49795_);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWER, 0).with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWER, POWERED);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERED) && state.get(FACING).getOpposite() != direction ? state.get(POWER) : 0;
    }

    @Override
    public int getComparatorOutput(BlockState pState, World pLevel, BlockPos pPos) {
        return pState.get(POWERED) ? 15 : 0;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, state.with(POWERED, false), Block.NOTIFY_LISTENERS);
        this.updateNeighbors(world, pos, state);
    }

    private void updateNeighbors(World world, BlockPos pos, BlockState bs){
        world.updateNeighborsAlways(pos, this);
        world.updateNeighborsAlways(pos.offset(bs.get(FACING).getOpposite()), this);
    }

    private void scheduleTick(WorldAccess world, BlockPos pos, int time) {
        if (!world.isClient()) {
            world.scheduleBlockTick(pos, this, time);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        // Dont do anything!
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        ArrayList<Iota> stack = new ArrayList<>(imageIn.getStack());

        if (stack.isEmpty()) {
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
                    MishapSpellCircleInvalidIota.ofType(last, 0, "int", pos)
            );
            return new ControlFlow.Stop();
        }
        var power = (int) ((DoubleIota) last).getDouble();
        if (power > 15 || power < 0) {
            this.fakeThrowMishap(
                    pos, bs, imageIn, env,
                    MishapSpellCircleInvalidIota.of(last, 0, "int.between", pos,15, 0)
            );
            return new ControlFlow.Stop();
        }
        var newbs = bs.with(POWER, power).with(POWERED, true);
        world.setBlockState(pos, newbs);
        this.updateNeighbors(world, pos, newbs);

        var imp = env.getImpetus();
        if (imp != null && imp.getExecutionState() != null){
            this.scheduleTick(world, pos,
                    ((ICircleSpeedValue) env.getImpetus().getExecutionState()).slate_work$getTickSpeed()
            );
        }

        var exitDirsSet = this.possibleExitDirections(pos, bs, world);
        exitDirsSet.remove(enterDir.getOpposite());
        var exits = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(pos, dir)).toList();

        return new ControlFlow.Continue(imageIn.copy(stack, imageIn.getParenCount(), imageIn.getParenthesized(),
                imageIn.getEscapeNext(), imageIn.getOpsConsumed(), imageIn.getUserData()), exits);
    }


    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return switch (pState.get(FACING)){
            case DOWN -> createCuboidShape(0.0, 16 -5.0, 0.0, 16.0, 16.0, 16.0);
            case UP -> createCuboidShape(0.0,0.0,0.0,16.0,5.0,16.0);
            case NORTH -> createCuboidShape(0.0, 0.0, 16 -5.0, 16.0, 16.0, 16.0);
            case SOUTH -> createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 5.0);
            case WEST -> createCuboidShape(16 -5.0, 0.0, 0.0, 16.0, 16.0, 16.0);
            case EAST -> createCuboidShape(0.0, 0.0, 0.0, 5.0, 16.0, 16.0);
        };
    }
}
