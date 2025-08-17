package org.sophia.slate_work.mixins;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import org.sophia.slate_work.registries.AttributeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayer {

    @Inject(at = @At("RETURN"), method = "createPlayerAttributes")
    private static void slate_work$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir){
        var out = cir.getReturnValue();
        out.add(AttributeRegistry.WHISPERING);
    }
}
