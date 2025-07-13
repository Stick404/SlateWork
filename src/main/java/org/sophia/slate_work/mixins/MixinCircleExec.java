package org.sophia.slate_work.mixins;


import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import org.sophia.slate_work.misc.CircleSpeedValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(CircleExecutionState.class)
public abstract class MixinCircleExec implements CircleSpeedValue {

    @Shadow public CastingImage currentImage;

    @Unique protected boolean slate_work$realValue = false;

    // Makes the next return of `CircleExecutionState#getTickSpeed` return its true speed
    @Override
    public void slate_work$getRealValue() {
        slate_work$realValue = true;
    }

    @Inject(method = "getTickSpeed", at = @At("TAIL"), cancellable = true, remap = false)
    protected void getTickSpeed(CallbackInfoReturnable<Integer> cir) {
        int targetSpeed = this.currentImage.getUserData().getInt("set_speed");

        if (targetSpeed != 0 && !slate_work$realValue) cir.setReturnValue(targetSpeed);
        if (slate_work$realValue) slate_work$realValue = false;
    }
}
