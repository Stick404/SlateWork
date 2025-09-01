package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleMedia;

public class AcceleratorLoci extends AbstractSlate {
    public static final int accel = 13;
    private static final double THICKNESS = 6;
    private static final VoxelShape DOWN_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, 16, THICKNESS, 16),
            Block.createCuboidShape(1,1,1,15,11,15));
    private static final VoxelShape UP_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 16 - THICKNESS, 0, 16, 16, 16),
            Block.createCuboidShape(1,5,1,15,15,15));
    private static final VoxelShape EAST_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, THICKNESS, 16, 16),
            Block.createCuboidShape(5,1,1,11,15,15));
    private static final VoxelShape WEST_AB = VoxelShapes.union(
            Block.createCuboidShape(16 - THICKNESS, 0, 0, 16, 16, 16),
            Block.createCuboidShape(5,1,1,15,15,15));
    private static final VoxelShape NORTH_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 16 - THICKNESS, 16, 16, 16),
            Block.createCuboidShape(1,1,5,15,15,15));
    private static final VoxelShape SOUTH_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, 16, 16, THICKNESS),
            Block.createCuboidShape(1,1,1,15,15,11));

    public AcceleratorLoci(Settings p_49795_) {
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
        var data = imageIn.getUserData().copy();
        var speed = data.getInt("accel_left");


        long cost = (MediaConstants.DUST_UNIT*2)*Math.max(1, (speed*speed)/accel);
        var extracted = env.extractMedia(cost, false);
        if (0L != extracted) {
            this.fakeThrowMishap(
                    pos, bs, imageIn, env,
                    new MishapSpellCircleMedia(extracted, pos)
            );
            return new ControlFlow.Stop();
        }

        data.putInt("accel_left", speed+accel);
        var exitDirsSet = this.possibleExitDirections(pos, bs, world);
        exitDirsSet.remove(enterDir.getOpposite());
        var exitDirs = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(pos, dir)).toList();
        return new ControlFlow.Continue(imageIn.copy(imageIn.getStack(),imageIn.getParenCount(),
                imageIn.getParenthesized(),imageIn.getEscapeNext(),imageIn.getOpsConsumed(), data), exitDirs);
    }
}
