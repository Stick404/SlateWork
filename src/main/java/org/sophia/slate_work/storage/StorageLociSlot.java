package org.sophia.slate_work.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.util.Pair;
import org.sophia.slate_work.blocks.entities.StorageLociEntity;

@SuppressWarnings("UnstableApiUsage")
public class StorageLociSlot implements SingleSlotStorage<ItemVariant>, StorageView<ItemVariant> {
    private final StorageLociEntity parent;
    private final int slot;

    public StorageLociSlot(StorageLociEntity parentP, int slotP){
        parent = parentP;
        slot = slotP;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return parent.insert(resource, maxAmount, transaction);
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return parent.extract(resource, maxAmount, transaction);
    }

    @Override
    public boolean isResourceBlank() {
        return stack().getLeft().isBlank();
    }

    @Override
    public ItemVariant getResource() {
        return stack().getLeft();
    }

    @Override
    public long getAmount() {
        return stack().getRight();
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    private Pair<ItemVariant,Long> stack(){
        return this.parent.getStack(this.slot);
    }
}
