package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ICircleComponent.class)
public interface MixinHahaWearAllCircleBlocks extends Equipment {
    @Override
    default EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }
}
