package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ICircleComponent.class)
public interface MixinICircleComponent {
    @Inject(method = "fakeThrowMishap", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    // Oh boy, holy shit
    default void onFakeThrow(BlockPos pos, BlockState bs, CastingImage image, CircleCastEnv env, Mishap mishap,
                               CallbackInfo ci, Mishap.Context errorCtx, OperatorSideEffect.DoMishap sideEffect,
                               CastingVM vm){
        if (env.getImpetus() != null)
            env.getImpetus().postMishap(mishap.errorMessageWithName(env, errorCtx));
        // Makes an Impetus *print* its error without the Spell Circle PR
        // https://github.com/Stick404/HexMod/tree/main
    }
}
