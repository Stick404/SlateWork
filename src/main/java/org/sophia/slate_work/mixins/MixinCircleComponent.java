package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sophia.slate_work.registries.SlateWorksImageComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ICircleComponent.class)
public interface MixinCircleComponent {

    @Inject(method = "sfx",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FF)V"),
            cancellable = true
    ) // A *few* extra vars
    private static void slate_work$shushYou(BlockPos pos, BlockState bs, World world, BlockEntityAbstractImpetus impetus, boolean success, CallbackInfo ci, @Local(name = "pitch") float pitch, @Local(name = "sound" )RegistryEntry<SoundEvent> sound){
        if (impetus != null){
            var image = impetus.getExecutionState().currentImage;
            var volume = image.getComponent(SlateWorksImageComponents.MUTE_LOCI);
            if (volume != null && volume != 1){
                world.playSound(null, pos, sound.value(), SoundCategory.BLOCKS, volume.floatValue(), pitch);
                ci.cancel();
            }
        }
    }
}
