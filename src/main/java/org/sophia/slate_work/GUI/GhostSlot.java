package org.sophia.slate_work.GUI;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
public class GhostSlot extends Slot {
    public GhostSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public void setStack(ItemStack stack) {
        if (this.getStack() != ItemStack.EMPTY) return;
        if (!stack.isEmpty()) stack = stack.copy();
        stack.setCount(1);
        this.inventory.setStack(this.index,stack);
    }

    @Override
    public void onQuickTransfer(ItemStack newItem, ItemStack original) {
        //super.onQuickTransfer(newItem, original);
    }

    @Override
    public ItemStack insertStack(ItemStack stack, int count) {
        this.setStack(stack);
        return stack;
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public ItemStack getStack() {
        var stack = super.getStack();
        return stack.copy();
    }

    @Override
    public ItemStack insertStack(ItemStack stack) {
        var copy = stack.copy();
        copy.setCount(1);
        this.inventory.setStack(this.index,copy);
        return stack;
    }

    @Override
    public ItemStack takeStack(int amount) { //Since this is a "ghost" we want to void the stack
        this.inventory.removeStack(this.index);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakePartial(PlayerEntity player) {
        return false;
    }

    @Override
    public ItemStack takeStackRange(int min, int max, PlayerEntity player) {
        return ItemStack.EMPTY;
    }
}
