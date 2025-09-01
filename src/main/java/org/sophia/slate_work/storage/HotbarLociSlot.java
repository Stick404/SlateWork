package org.sophia.slate_work.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.sophia.slate_work.blocks.entities.HotbarLociEntity;

@SuppressWarnings("UnstableApiUsage")
public class HotbarLociSlot implements SingleSlotStorage<ItemVariant>, StorageView<ItemVariant> {
    private final HotbarLociEntity parent;
    private final int slot;

    public HotbarLociSlot(HotbarLociEntity parentP, int slotP){
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
        return parent.getSlotStack(slot).isEmpty();
    }

    @Override
    public ItemVariant getResource() {
        return ItemVariant.of(parent.getSlotStack(slot));
    }

    @Override
    public long getAmount() {
        return parent.getSlotStack(slot).getCount();
    }

    @Override
    public long getCapacity() {
        return 64;
    }
}
