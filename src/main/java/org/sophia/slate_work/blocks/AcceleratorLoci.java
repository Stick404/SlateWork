package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.misc.MediaConstants;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleMedia;

public class AcceleratorLoci extends AbstractSlate {
    public static final int accel = 13;
    public AcceleratorLoci(Settings p_49795_) {
        super(p_49795_);
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
