package org.sophia.slate_work.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class LociIterator<T extends SlottedStorage<ItemVariant>> implements Iterator<StorageView<ItemVariant>> {
    private int index = 0;
    private final T entity;
    public LociIterator(T entityP){
        entity = entityP;
    }

    @Override
    public boolean hasNext() {
        return index <= entity.getSlotCount();
    }

    @Override
    public StorageView<ItemVariant> next() {
        var old = index++;
        return entity.getSlot(old);
    }
}
