package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CircleCastEnv.class)
public abstract class MixinCircleEnv {
    @Shadow public abstract CircleExecutionState circleState();

    @Shadow @Final protected CircleExecutionState execState;

    @Shadow public abstract @Nullable BlockEntityAbstractImpetus getImpetus();

    @Redirect(method = "postExecution(Lat/petrak/hexcasting/api/casting/eval/CastResult;)V",
            at = @At(value = "INVOKE",
                    target = "Lat/petrak/hexcasting/api/casting/eval/sideeffects/EvalSound;sound()Lnet/minecraft/sound/SoundEvent;"
            )
    )
    public SoundEvent slate_work$shushYouToo(EvalSound instance){
        var image = this.circleState().currentImage;
        var volume = image.getUserData().getFloat("volume");
        var mute = image.getUserData().getBoolean("mute");
        if (mute && this.getImpetus() != null && volume != 0 && instance.sound() != null){
            if (this.getImpetus().getWorld() != null) {
                BlockPos soundPos = this.execState.currentPos;
                this.getImpetus().getWorld().playSound(null, soundPos, instance.sound(), SoundCategory.PLAYERS, volume, 1.0F);
            }
        }
        return null;
    }
}
