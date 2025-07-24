package org.sophia.slate_work.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.sophia.slate_work.blocks.entities.CraftingLociEntity;

// So mods that interact with Inventory cant just slurp items out of the Crafting Locus without permission
public class DumbDumbInv implements Inventory {
    private final CraftingLociEntity parent;

    public DumbDumbInv(CraftingLociEntity parent){
        this.parent = parent;
    }

    @Override
    public int size() {
        return 10;
    }

    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return false;
    }

    public boolean isEmpty() {
        return false; // Hmm, might be wrong...
    }

    public ItemStack getStack(int slot) {
        return parent.getStack(slot);
    }

    public ItemStack removeStack(int slot, int amount) {
        return parent.removeStack(slot);
    }

    public ItemStack removeStack(int slot) {
        return parent.removeStack(slot);
    }

    public void setStack(int slot, ItemStack stack) {
        this.parent.setStack(slot,stack);
    }

    @Override
    public void markDirty() {
        this.parent.markDirty();
    }

    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    public void clear() {
        this.parent.clear();
    }
}
