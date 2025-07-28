package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import net.minecraft.block.WearableCarvedPumpkinBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockAbstractImpetus.class)
public class MixinBlockAbstractImpetus implements Equipment {
    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }
}
