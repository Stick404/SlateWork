package org.sophia.slate_work.GUI;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.sophia.slate_work.blocks.entities.HotbarLociEntity;

@SuppressWarnings("UnstableApiUsage")
public class FakeInvHotbarLoci implements Inventory {
    final HotbarLociEntity parent;

    public FakeInvHotbarLoci(HotbarLociEntity parent) {
        this.parent = parent;
    }

    @Override
    public int size() {
        return parent.getSlotCount();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getStack(int slot) {
        return parent.getSlotStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return parent.removeStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return parent.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        parent.setStack(slot, stack);
    }

    @Override
    public void markDirty() {
        parent.sync();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
    }
}
