package org.sophia.slate_work.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import org.sophia.slate_work.blocks.entities.StorageLociEntity;

public class StorageLociSlot implements SingleSlotStorage<ItemVariant>, StorageView<ItemVariant> {
    private final StorageLociEntity parent;
    private final int slot;

    public StorageLociSlot(StorageLociEntity parent, int slot){
        this.parent = parent;
        this.slot = slot;
        this.variant = ItemVariant.blank();
        this.count = 0;
    }

    public StorageLociSlot(StorageLociEntity parent, int slot, ItemVariant variant, long count){
        this.parent = parent;
        this.slot = slot;
        this.variant = variant;
        this.count = count;
    }

    @Override
    public StorageLociSlot createSnapshot() {
        return new StorageLociSlot(this.parent, this.slot, this.variant, this.count);
    }

    @Override
    protected void readSnapshot(StorageLociSlot snapshot) {
        this.variant = snapshot.variant;
        this.count = snapshot.count;
    }

    @Override
    protected void onFinalCommit() {
        this.parent.setStack(this.slot, this.variant, this.count);
    }


    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.equals(this.variant) || this.isResourceBlank()) {
            updateSnapshots(transaction);
            // We do this in case we are inserting into a blank slot
            this.variant = resource;
            this.count += maxAmount;
            return maxAmount;
        }

        return 0;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.equals(this.variant)) {
            updateSnapshots(transaction);
            this.count -= maxAmount;
            return maxAmount;
        }

        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return this.count <= 0 || this.variant.isBlank();
    }

    @Override
    public ItemVariant getResource() {
        return this.variant;
    }

    @Override
    public long getAmount() {
        return this.count;
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }
}
