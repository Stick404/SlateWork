package org.sophia.slate_work.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.TagKey;
import org.sophia.slate_work.blocks.entities.HotbarLociEntity;

import java.util.Set;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public class SlateFakePlayerInv extends PlayerInventory {
    private final HotbarLociEntity hotbarLociEntity;

    public SlateFakePlayerInv(PlayerEntity player, HotbarLociEntity hotbarLociEntity) {
        super(player);
        this.hotbarLociEntity = hotbarLociEntity;
    }

    @Override
    public boolean contains(TagKey<Item> tag) {
        for (ItemStack itemStack : this.hotbarLociEntity.getStacks()) {
            if (!itemStack.isEmpty() && itemStack.isIn(tag)) {
                return true;
            }
        }

        return false;
    }

    public HotbarLociEntity getHotbarLociEntity() {
        return hotbarLociEntity;
    }

    @Override
    public int getEmptySlot() {
        return hotbarLociEntity.getFreeSlot(ItemVariant.blank());
    }

    @Override
    public int getSlotWithStack(ItemStack stack) {
        return hotbarLociEntity.getFreeSlot(ItemVariant.of(stack));
    }

    @Override
    public int indexOf(ItemStack stack) {
        int i = 0;
        for (ItemStack itemStack : this.hotbarLociEntity.getStacks()) {
            if (itemStack.equals(stack)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot < 0 || slot >= hotbarLociEntity.getSlotCount()) {
            return ItemStack.EMPTY;
        }
        return hotbarLociEntity.getSlotStack(slot);
    }

    // TODO: see if we need to do this?
    @Override
    public int getOccupiedSlotWithRoomForStack(ItemStack stack) {
        return super.getOccupiedSlotWithRoomForStack(stack);
    }

    @Override
    public boolean insertStack(int slot, ItemStack stack) {
        Transaction trans =Transaction.openOuter();
        var count = hotbarLociEntity.insert(ItemVariant.of(stack), stack.getCount(), trans);
        trans.commit();
        return count > 0;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return hotbarLociEntity.removeStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return hotbarLociEntity.removeStack(slot, amount);
    }

    @Override
    public void removeOne(ItemStack stack) {
        int slot = this.getSlotWithStack(stack);
        if (slot != -1) {
            hotbarLociEntity.removeStack(slot, 1);
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        hotbarLociEntity.setStack(slot, stack);
    }

    @Override
    public NbtList writeNbt(NbtList nbtList) {
        return nbtList;
    }

    @Override
    public void readNbt(NbtList nbtList) {

    }

    @Override
    public int size() {
        return hotbarLociEntity.getSlotCount();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.hotbarLociEntity.getStacks()) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(ItemStack stack) {
        return hotbarLociEntity.getFreeSlot(ItemVariant.of(stack)) != -1;
    }

    @Override
    public boolean containsAny(Set<Item> items) {
        return hotbarLociEntity.getInv().containsAny(items);
    }

    @Override
    public boolean containsAny(Predicate<ItemStack> predicate) {
        return hotbarLociEntity.getInv().containsAny(predicate);
    }
}
