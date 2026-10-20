package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
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

public class RedstoneLoci extends AbstractSlate {
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty RECEIVE = BooleanProperty.of("receive");
    public static final IntProperty POWER = Properties.POWER;

    @Override
    public Direction normalDir(BlockPos pos, BlockState bs, World world, int recursionLeft) {
        return super.normalDir(pos, bs, world, recursionLeft);
    }

    public RedstoneLoci(Settings p_49795_) {
        super(p_49795_);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWER, 0).with(POWERED, false).with(WATERLOGGED, false).with(RECEIVE, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWER, POWERED, RECEIVE);
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
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        world.setBlockState(pos, state.with(RECEIVE, !state.get(RECEIVE)));
        world.playSound(player, pos, HexSounds.FLIGHT_FINISH.value(), SoundCategory.BLOCKS, 1.0f, 1f);
        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if (state.get(RECEIVE)){
            int power = world.getReceivedRedstonePower(pos);
            if (power > 0) {
                world.setBlockState(pos, state.with(POWER, power));
            }
        }
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        var stack = imageIn.getStack();
        var exitDirsSet = this.possibleExitDirections(pos, bs, world);
        exitDirsSet.remove(enterDir.getOpposite());
        var exits = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(pos, dir)).toList();

        var imp = env.getImpetus();
        if (imp != null && imp.getExecutionState() != null){
            this.scheduleTick(world, pos,
                    ((ICircleSpeedValue) env.getImpetus().getExecutionState()).slate_work$getTickSpeed()
            );
        }

        if (bs.get(RECEIVE)) {
            int power = world.getReceivedRedstonePower(pos);
            stack = stack.appended(new DoubleIota(power));
            var newbs = bs.with(POWER, power).with(POWERED, true);
            world.setBlockState(pos, newbs);

            return new ControlFlow.Continue(imageIn.copy(stack, imageIn.getParenCount(), imageIn.getParenthesized(),
                    imageIn.getEscapeNext(), imageIn.getSimulateNext(), imageIn.getOpsConsumed(), imageIn.getComponents()), exits);
        }

        if (stack.isEmpty()) {
            this.fakeThrowMishap(
                    pos, bs, imageIn, env,
                    new MishapSpellCircleNotEnoughArgs(1,0, pos)
            );
            return new ControlFlow.Stop();
        }

        var last = stack.last();
        stack = stack.init();

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

        return new ControlFlow.Continue(imageIn.copy(stack, imageIn.getParenCount(), imageIn.getParenthesized(),
                imageIn.getEscapeNext(), imageIn.getSimulateNext(), imageIn.getOpsConsumed(), imageIn.getComponents()), exits);
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
