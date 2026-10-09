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
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
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

import static org.sophia.slate_work.registries.SlateWorksBlockRegistry.STORAGE_LOCI_ENTITY;

// So this almost works like a fucked up Inventory. Instead of ItemStacks, it uses a pair of ItemStack (for the type)
// and a Long for the real amount held. Janky? Yes, should work? Hope so!
public class StorageLociEntity extends HexBlockEntity implements SlottedStorage<ItemVariant> {
    private static final Pair<ItemVariant, Long> emptySlot = new Pair<>(ItemVariant.blank(), 0L);
    private final Pair<ItemVariant, Long>[] slots = DefaultedList.ofSize(16, emptySlot).toArray(new Pair[16]);
    // Java, please, I just want an array of ItemStack.EMPTY at first

    public StorageLociEntity(BlockPos pos, BlockState state) {
        super(STORAGE_LOCI_ENTITY, pos, state);
        this.slots = new StorageLociSlot[16];
        for (int i = 0; i < 16; i++) {
            this.slots[i] = new StorageLociSlot(this, i);
        }
    }

    @Override
    protected void saveModData(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList nbtList = new NbtList();

        for(int i = 0; i < this.slots.length; ++i) {
            ItemVariant stack = slots[i].getResource();
            if (!stack.isBlank()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte) i);
                nbtCompound.put("Item", ItemVariant.CODEC.encodeStart(NbtOps.INSTANCE, stack).getOrThrow());
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
    protected void loadModData(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        var items = nbt.getList("Items", NbtElement.COMPOUND_TYPE);

        if (nbt.getBoolean("Empty")){
            this.clear();
        }

        for (int i = 0; i < this.slots.length; ++i) {
            NbtCompound compound = items.getCompound(i);
            var item = ItemVariant.CODEC.decode(NbtOps.INSTANCE, compound.getCompound("Item"));
            Pair<ItemVariant,Long> stack;
            if (item.isError()) {
                stack = new Pair<>(ItemVariant.blank(), 0L);
            } else {
                stack = new Pair<>(item.getOrThrow().getFirst(), compound.getLong("Count"));;
            }

            this.slots[i] = stack;
        }
    }

    public boolean isEmpty() {
        for (var z : this.slots){
            if (!z.isResourceBlank()) return false;
        }
        return true;
    }

    // Returns the slot found empty, else returns -1 if the Locus is full
    public int isFull(){
        int i = 0;
        for (var z : this.slots){
            if (z.isResourceBlank()) return i;
            i++;
        }
        return -1;
    }


    /**
     *  Returns a *copy*
     *  **/
    public Pair<ItemVariant,Long> getStack(int slot) {
        return new Pair<>(this.slots[slot].getResource(), this.slots[slot].getAmount());
    }

    /// You better know what you are doing...
    public StorageLociSlot[] getInventory(){
        return this.slots;
    }

    /// Mutates the storage locus!
    public Pair<ItemVariant,Long> removeStack(int slot, int amount) {
        if (amount <= 0) return new Pair<>(ItemVariant.blank(), 0L); //Pain.
        StorageLociSlot storageSlot = this.slots[slot];
        long returned;

        if (storageSlot.isResourceBlank()){
            this.slots[slot] = new StorageLociSlot(this, slot);
            return new Pair<>(ItemVariant.blank(), 0L);
        }
        if (storageSlot.getAmount() <= amount) {
            returned = storageSlot.getAmount();;
            this.slots[slot] = new StorageLociSlot(this, slot);
        } else {
            long slotHeld = storageSlot.getAmount();
            this.slots[slot] = new StorageLociSlot(this, slot, storageSlot.getResource(), slotHeld -amount);
            returned = amount;
        }
        this.sync();
        return new Pair<>(storageSlot.getResource(), returned);
    }

    public @Nullable Integer getSlot(ItemVariant item){
        for (int i = 0; i < slots.length; i++) {
            var stored = this.slots[i].getLeft();
            if (item.getItem() == stored.getItem() && item.matches(stored.toStack())) {
                return i;
            }
        }
        this.sync();
        return null;
    }

    public Pair<ItemVariant,Long> removeStack(int slot) {
        StorageLociSlot storageSlot = this.slots[slot];
        if (!storageSlot.isResourceBlank()) {
            this.slots[slot] = new StorageLociSlot(this, slot);
        }
        this.sync();
        return new Pair<>(storageSlot.getResource(), storageSlot.getAmount());
    }

    public void setStack(int slot, ItemVariant stack, long amount) {
        if (amount <= 0){ // If this somehow comes from overflow, you kind of deserve it
            this.slots[slot] = new StorageLociSlot(this, slot, ItemVariant.blank(), 0L);
            this.sync();
            return;
        }
        this.slots[slot] = new StorageLociSlot(this, slot, stack, amount);
        this.sync();
    }

    public void setStack(int slot, Pair<ItemVariant, Long> pair) {
        this.setStack(slot, pair.getLeft(), pair.getRight());
    }

    public void clear() {
        for (int i = 0; i < 16; i++) {
            this.slots[i] = new StorageLociSlot(this, i);
        }
        if (this.world != null) this.sync();
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        var slotT = getSlot(resource);
        if (slotT == null) slotT = this.isFull();
        if (slotT  == -1) return 0;
        int slot = slotT;

        transaction.addCloseCallback((context, z) -> {
            var stack = getStack(slot);
            if (z.wasCommitted()) {
                if (stack.getLeft().isBlank())
                    this.setStack(slot, new Pair<>(resource, maxAmount));
                else
                    this.setStack(slot, new Pair<>(stack.getLeft(), stack.getRight() + maxAmount));
                this.sync();
            }
        });
        return maxAmount;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        var slot = getSlot(resource);
        if (slot == null) return 0;
        if (maxAmount <= 0) return 0;

        StorageLociSlot storageSlot = this.slots[slot];
        long returned = Math.min(storageSlot.getAmount(), maxAmount);

        if (copy == ItemVariant.blank() || pair.getRight() == 0){
            this.slots[slot] = emptySlot;
        }
        if (pair.getRight() <= maxAmount) {
            returned = pair.getRight();
        } else {
            returned = maxAmount;
        }
        this.markDirty();
        transaction.addCloseCallback((context, z) -> {
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
        return this.slots[slot];
    }

    @Override
    public @UnmodifiableView List<SingleSlotStorage<ItemVariant>> getSlots() {
        return SlottedStorage.super.getSlots();
    }
}
