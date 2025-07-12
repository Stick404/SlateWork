package org.sophia.slate_work.GUI;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.Optional;

public class GhostSlotOutput extends GhostSlot {
    ScreenHandler handler;
    public GhostSlotOutput(Inventory inventory, int index, int x, int y, ScreenHandler handler) {
        super(inventory, index, x, y);
        this.handler = handler;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public void setStack(ItemStack stack) {
        // Do nothing, this is just for display/interaction
    }

    @Override
    public ItemStack takeStack(int amount) {
        this.inventory.clear();
        return ItemStack.EMPTY;
    }

    @Override
    public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
        this.takeStack(1);
        return Optional.empty();
    }

    @Override
    public int getMaxItemCount() {
        return 64;
    }
}
