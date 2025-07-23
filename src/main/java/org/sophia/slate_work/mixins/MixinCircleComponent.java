package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.block.BlockState;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;

@Mixin(ICircleComponent.class)
public interface MixinCircleComponent {
    @Inject(method = "sfx",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    ) // A *few* extra vars
    private static void slate_work$shushYou(BlockPos pos, BlockState bs, World world, BlockEntityAbstractImpetus impetus, boolean success, CallbackInfo ci, Vec3d vpos, Vec3d vecOutDir, FrozenPigment colorizer, UUID activator, float pitch, SoundEvent sound){
        if (impetus != null){
            var image = impetus.getExecutionState().currentImage;
            var volume = image.getUserData().getFloat("volume");
            var mute = image.getUserData().getBoolean("mute");
            if (mute){
                world.playSound(null, vpos.x, vpos.y, vpos.z, sound, SoundCategory.BLOCKS, volume, pitch);
                ci.cancel();
            }
        }
    }
}
