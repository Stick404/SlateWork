package org.sophia.slate_work.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static org.sophia.slate_work.registries.BlockRegistry.STORAGE_LOCI_ENTITY;

public class StorageLociEntity extends BlockEntity implements Inventory {
    private int slotCount = 15; ///  The amount of "types" this can store. This includes
    private static final Pair<ItemStack,Long> emptySlot = new Pair<>(ItemStack.EMPTY, 0L);
    private Pair<ItemStack,Long>[] slots = DefaultedList.ofSize(this.slotCount, emptySlot).toArray(new Pair[slotCount]);
    // Java, please, I just want an array of ItemStack.EMPTY at first

    public StorageLociEntity(BlockPos pos, BlockState state) {
        super(STORAGE_LOCI_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtList nbtList = new NbtList();

        for(int i = 0; i < this.slots.length; ++i) {
            ItemStack itemStack = slots[i].getLeft();
            if (!itemStack.isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte)i);
                itemStack.writeNbt(nbtCompound);
                nbtCompound.putLong("RealCount",slots[i].getRight());
                nbtList.add(nbtCompound);
            }
        }
        if (!nbtList.isEmpty()) {
            nbt.put("Items", nbtList);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        var items = nbt.getList("Items", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < this.slots.length; ++i) {
            NbtCompound compound = items.getCompound(i);
            Pair<ItemStack,Long> stack = new Pair<>(ItemStack.fromNbt(compound),(compound.getLong("RealCount")));
            this.slots[i] = stack;
        }
    }

    @Override
    public int size() {
        return slotCount;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getStack(int slot) {
        var pair = this.slots[slot];
        var copy = pair.getLeft().copy();
        if (pair.getRight() >= Integer.MAX_VALUE){
            copy.setCount(Integer.MAX_VALUE);
        } else {
            copy.setCount(pair.getRight().intValue());
        }

        return copy;
    }

    //TODO: MAKE THIS RESPECT THE ITEM'S MAX
    @Override
    public ItemStack removeStack(int slot, int amount) {
        var pair = this.slots[slot];
        var copy = pair.getLeft().copy();

        if (amount > pair.getRight()){
            var pairNew = new Pair<>(ItemStack.EMPTY,0L);
            this.slots[slot] = pairNew;
            copy.setCount(pair.getRight().intValue());
            return copy;
        }
        pair.setRight(pair.getRight() -amount);
        copy.setCount(amount);
        return copy;
    }

    //TODO: MAKE THIS RESPECT THE ITEM'S MAX
    @Override
    public ItemStack removeStack(int slot) {
        var pair = this.slots[slot];
        var stack = pair.getLeft().copy();
        var maxCount = stack.getMaxCount();

        if (pair.getRight() < maxCount){
            pair.setLeft(ItemStack.EMPTY);
            stack.setCount(pair.getRight().intValue());
            pair.setRight(0L);
            return stack;
        }

        stack.setCount(maxCount);
        pair.setRight(pair.getRight()-maxCount);
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.slots[slot] = new Pair<>(stack, (long) stack.getCount());
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public int getMaxCountPerStack() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void clear() {
    }
}
