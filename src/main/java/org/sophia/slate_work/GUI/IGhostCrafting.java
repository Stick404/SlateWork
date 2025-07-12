package org.sophia.slate_work.GUI;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface IGhostCrafting extends Inventory {

    void setCraftSlot(ItemStack stack);
}
