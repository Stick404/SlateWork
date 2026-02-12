package org.sophia.slate_work.mixins;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.sophia.slate_work.registries.AttributeRegistry;
import org.sophia.slate_work.storage.SlateFakePlayer;
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

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void slate_work$dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        if ((Object)this instanceof SlateFakePlayer player && !stack.isEmpty()) {

            var pos = player.getInventory().getHotbarLociEntity().getPos().toCenterPos();
            ItemEntity itemEntity = new ItemEntity(player.getWorld(), pos.getX(), pos.getY(), pos.getZ(), stack);
            itemEntity.setPickupDelay(40);

            cir.setReturnValue(itemEntity);
            cir.cancel();
        }
    }
}
