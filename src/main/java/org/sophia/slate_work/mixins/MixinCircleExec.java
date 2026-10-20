package org.sophia.slate_work.mixins;


import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import org.sophia.slate_work.misc.ICircleSpeedValue;
import org.sophia.slate_work.registries.SlateWorksImageComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(CircleExecutionState.class)
public abstract class MixinCircleExec implements ICircleSpeedValue {

    @Shadow public CastingImage currentImage;

    @Shadow
    protected abstract int getTickSpeed();

    @Unique protected boolean slate_work$realValue = false;

    // Makes the nex return of `CircleExecutionState#getTickSpeed` return its true speed
    @Override
    public void slate_work$getRealValue() {
        slate_work$realValue = true;
    }

    @Override
    public int slate_work$getTickSpeed() {
        return this.getTickSpeed();
    }

    @Inject(method = "getTickSpeed", at = @At("TAIL"), cancellable = true, remap = false)
    protected void Slate_work$realValue(CallbackInfoReturnable<Integer> cir) {
        var targetSpeed = this.currentImage.getComponent(SlateWorksImageComponents.SPEED_LOCI);

        if (targetSpeed != null && targetSpeed == 0 && !slate_work$realValue){
            var accelLeft = this.currentImage.getComponent(SlateWorksImageComponents.ACCELERATOR_LOCI);
            if (accelLeft != null && accelLeft > 0){
                this.currentImage = this.currentImage.withComponent(SlateWorksImageComponents.ACCELERATOR_LOCI, -accelLeft);

                var img = this.currentImage;
                this.slate_work$setImage(img.copy(img.getStack(), img.getParenCount(), img.getParenthesized(),
                        img.getEscapeNext(), img.getSimulateNext(), img.getOpsConsumed(), img.getComponents()));

                this.slate_work$getRealValue();
                int currentSpeed = this.getTickSpeed();
                cir.setReturnValue(Math.max(currentSpeed -accelLeft, 1));
                return;
            }
        }
        if (targetSpeed != null && targetSpeed != 0 && !slate_work$realValue) {
            if (cir.getReturnValue() < targetSpeed){
                cir.setReturnValue(targetSpeed);
            }
        }
        if (slate_work$realValue) slate_work$realValue = false;
    }

    @Unique
    public void slate_work$setImage(CastingImage image){
        this.currentImage = image;
    }
}
