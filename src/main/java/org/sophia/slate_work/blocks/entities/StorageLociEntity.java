package org.sophia.slate_work.blocks.entities;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.sophia.slate_work.storage.LociIterator;
import org.sophia.slate_work.storage.StorageLociSlot;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.sophia.slate_work.registries.BlockRegistry.STORAGE_LOCI_ENTITY;

// So this almost works like a fucked up Inventory. Instead of ItemStacks, it uses a pair of ItemStack (for the type)
// and a Long for the real amount held. Janky? Yes, should work? Hope so!
@SuppressWarnings(value = "UnstableApiUsage")
public class StorageLociEntity extends HexBlockEntity implements SlottedStorage<ItemVariant> {
    private static final Pair<ItemVariant,Long> emptySlot = new Pair<>(ItemVariant.blank(), 0L);
    private final Pair<ItemVariant,Long>[] slots = DefaultedList.ofSize(16, emptySlot).toArray(new Pair[16]);
    // Java, please, I just want an array of ItemStack.EMPTY at first

    public StorageLociEntity(BlockPos pos, BlockState state) {
        super(STORAGE_LOCI_ENTITY, pos, state);
    }

    @Override
    protected void saveModData(NbtCompound nbt) {
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
            nbt.putBoolean("Empty", false); // *Just* in case...
        } else {
            nbt.putBoolean("Empty", true);
        }
    }

    @Override
    protected void loadModData(NbtCompound nbt) {
        var items = nbt.getList("Items", NbtElement.COMPOUND_TYPE);

        if (nbt.getBoolean("Empty")){
            this.clear();
        }

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


    /**
     *  Returns a *copy*
     *  **/
    public Pair<ItemVariant,Long> getStack(int slot) {
        return new Pair<>(this.slots[slot].getLeft(),this.slots[slot].getRight());
    }

    // You better know what you are doing...
    public Pair<ItemVariant,Long>[] getInventory(){
        return this.slots;
    }

    public Pair<ItemVariant,Long> removeStack(int slot, int amount) {
        if (amount <= 0) return emptySlot; //Pain.
        var pair = this.slots[slot];
        var copy = pair.getLeft();
        long returned;

        if (copy == ItemVariant.blank() || pair.getRight() == 0){
            this.slots[slot] = emptySlot;
            return emptySlot;
        }
        if (pair.getRight() <= amount) {
            returned = pair.getRight();
            this.slots[slot] = emptySlot;
        }
        else {
            this.slots[slot].setRight(this.slots[slot].getRight() - amount);
            returned = amount;
        }
        this.sync();
        return new Pair<>(copy,returned);
    }

    public @Nullable Integer getSlot(ItemVariant item){
        for (int i = 0; i < slots.length; i++) {
            if (item.getItem() == this.slots[i].getLeft().getItem())
                    return i;
        }
        this.sync();
        return null;
    }

    public Pair<ItemVariant,Long> removeStack(int slot) {
        var pair = this.slots[slot];
        var copy = pair.getLeft();
        if (!copy.isBlank()) {
            this.slots[slot] = new Pair<>(emptySlot.getLeft(), emptySlot.getRight());
        }
        this.sync();
        return new Pair<>(copy,pair.getRight());
    }

    public void setStack(int slot, ItemVariant stack, long amount) {
        if (amount <= 0){ // If this somehow comes from overflow, you kind of deserve it
            this.slots[slot] = new Pair<>(emptySlot.getLeft(), emptySlot.getRight());
            this.sync();
            return;
        }
        this.slots[slot] = new Pair<>(stack, amount);
        this.sync();
    }

    public void setStack(int slot, Pair<ItemVariant, Long> pair) {
        this.setStack(slot, pair.getLeft(), pair.getRight());
    }

    public void clear() {
        Arrays.fill(this.slots, new Pair<>(emptySlot.getLeft(),emptySlot.getLeft()));
        if (this.world != null) this.sync();
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        var slot = getSlot(resource);
        if (slot == null) slot = this.isFull();
        if (slot  == -1) return 0;

        var stack = getStack(slot);
        if (stack.getLeft().isBlank()) this.setStack(slot, new Pair<>(resource, maxAmount));
        else this.setStack(slot, new Pair<>(stack.getLeft(), stack.getRight() + maxAmount));
        this.markDirty();
        return maxAmount;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        var slot = getSlot(resource);
        if (slot == null) return 0;
        if (maxAmount <= 0) return 0;

        var pair = this.slots[slot];
        var copy = pair.getLeft();
        long returned;

        if (copy == ItemVariant.blank() || pair.getRight() == 0){
            this.slots[slot] = emptySlot;
        }
        if (pair.getRight() <= maxAmount) {
            returned = pair.getRight();
        }
        else {
            returned = maxAmount;
        }
        this.markDirty();
        transaction.addCloseCallback((a,z) -> {
            if (z.wasCommitted()) {
                this.removeStack(slot, (int) maxAmount);
            }
        });
        return returned;
    }

    @Override
    public @NotNull Iterator<StorageView<ItemVariant>> iterator() {
        return new LociIterator<>(this);
    }

    @Override
    public int getSlotCount() {
        return 16;
    }

    @Override
    public StorageLociSlot getSlot(int slot) {
        return new StorageLociSlot(this, slot);
    }

    @Override
    public @UnmodifiableView List<SingleSlotStorage<ItemVariant>> getSlots() {
        return SlottedStorage.super.getSlots();
    }
}
