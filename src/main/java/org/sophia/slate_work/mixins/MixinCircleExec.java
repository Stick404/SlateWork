package org.sophia.slate_work.mixins;


import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(CircleExecutionState.class)
public abstract class MixinCircleExec {

    @Shadow
    public CastingImage currentImage;

    @Inject(method = "getTickSpeed", at = @At("TAIL"), cancellable = true, remap = false)
    protected void getTickSpeed(CallbackInfoReturnable<Integer> cir) {
        int targetSpeed = this.currentImage.getUserData().getInt("set_speed");
        if (targetSpeed != 0 ) cir.setReturnValue(targetSpeed);
    }

    @Invoker("getTickSpeed")
    public abstract int getTickSpeed();
}
