package org.sophia.slate_work.blocks.entities;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import static org.sophia.slate_work.registries.BlockRegistry.STORAGE_LOCI_ENTITY;

// So this almost works like a fucked up Inventory. Instead of ItemStacks, it uses a pair of ItemStack (for the type)
// and a Long for the real amount held. Janky? Yes, should work? Hope so!
@SuppressWarnings(value = "UnstableApiUsage")
public class StorageLociEntity extends BlockEntity {
    private static final Pair<ItemVariant,Long> emptySlot = new Pair<>(ItemVariant.blank(), 0L);
    private Pair<ItemVariant,Long>[] slots = DefaultedList.ofSize(16, emptySlot).toArray(new Pair[16]);
    // Java, please, I just want an array of ItemStack.EMPTY at first

    public StorageLociEntity(BlockPos pos, BlockState state) {
        super(STORAGE_LOCI_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtList nbtList = new NbtList();

        for(int i = 0; i < this.slots.length; ++i) {
            ItemVariant stack = slots[i].getLeft();
            if (!stack.isBlank()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte) i);
                nbtCompound.put("Item", stack.toNbt());
                nbtCompound.putLong("Count",slots[i].getRight());
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
            Pair<ItemVariant,Long> stack = new Pair<>(ItemVariant.fromNbt(compound.getCompound("Item")),(compound.getLong("Count")));
            this.slots[i] = stack;
        }
    }


    public boolean isEmpty() {
        for (var z : this.slots){
            if (!z.getLeft().isBlank() || z.getRight() != 0) return false;
        }
        return true;
    }

    // Returns the slot found empty, else returns -1 if the Locus is full
    public int isFull(){
        int i = 0;
        for (var z : this.slots){
            if (z.getLeft().isBlank() || z.getRight() == 0) return i;
            i++;
        }
        return -1;
    }


    public Pair<ItemVariant,Long> getStack(int slot) {
        return this.slots[slot];
    }

    // You better know what you are doing...
    public Pair<ItemVariant,Long>[] getInventory(){
        return this.slots;
    }

    public Pair<ItemVariant,Long> removeStack(int slot, int amount) {
        var pair = this.slots[slot];
        var copy = pair.getLeft();
        long returned;

        if (copy == ItemVariant.blank() || pair.getRight() == 0){
            this.slots[slot] = emptySlot;
            return this.slots[slot];
        }
        if (pair.getRight() <= amount) {
            returned = pair.getRight();
            this.slots[slot] = emptySlot;
        }
        else {
            this.slots[slot].setRight(this.slots[slot].getRight() - amount);
            returned = amount;
        }

        return new Pair<>(copy,returned);
    }

    public @Nullable Integer getSlot(ItemVariant item){
        for (int i = 0; i < slots.length; i++) {
            if (item.getItem() == this.slots[i].getLeft().getItem())
                    return i;
        }
        return null;
    }

    public Pair<ItemVariant,Long> removeStack(int slot) {
        var pair = this.slots[slot];
        var copy = pair.getLeft();
        if (copy == ItemVariant.blank() || pair.getRight() == 0){
            this.slots[slot] = emptySlot;
            return emptySlot;
        }
        return new Pair<>(copy,pair.getRight());
    }
    @SuppressWarnings(value = "UnstableApiUsage")
    public void setStack(int slot, ItemVariant stack, long amount) {
        this.slots[slot] = new Pair<>(stack, amount);
    }

    public void clear() {
        this.slots = DefaultedList.ofSize(16, emptySlot).toArray(new Pair[16]);
    }
}
