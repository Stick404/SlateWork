package org.sophia.slate_work.blocks.entities;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.GUI.Ghost3x3ScreenHandler;
import org.sophia.slate_work.misc.DumbDumbInv;
import org.sophia.slate_work.storage.HotbarLociSlot;

import java.util.Iterator;

import static org.sophia.slate_work.registries.BlockRegistry.CRAFTING_LOCI_ENTITY;

public class CraftingLociEntity extends BlockEntity implements ExtendedScreenHandlerFactory, SlottedStorage<ItemVariant> {
    DefaultedList<ItemStack> inv = DefaultedList.ofSize(10,ItemStack.EMPTY);

    public CraftingLociEntity(BlockPos pos, BlockState state) {
        super(CRAFTING_LOCI_ENTITY, pos, state);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.slate_work.crafting_loci");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new Ghost3x3ScreenHandler(syncId, playerInventory, new DumbDumbInv(this));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt,this.inv);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt,this.inv);
    }

    public ItemStack getStack(int slot) {
        return inv.get(slot);
    }

    public ItemStack removeStack(int slot) {
        this.inv.set(slot, ItemStack.EMPTY);
        this.markDirty();
        return ItemStack.EMPTY;
    }

    public void setStack(int slot, ItemStack stack) {
        stack = stack.copy();
        this.inv.set(slot,stack);
        this.markDirty();
    }

    public void clear() {
        this.inv = DefaultedList.ofSize(10,ItemStack.EMPTY);
        this.markDirty();
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public int getSlotCount() {
        return 0;
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        return new SingleSlotStorage<>() { // Tell it *no*, nothing here, nothing there
            @Override
            public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                return 0;
            }

            @Override
            public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                return 0;
            }

            @Override
            public boolean isResourceBlank() {
                return true;
            }

            @Override
            public ItemVariant getResource() {
                return ItemVariant.of(Items.AIR);
            }

            @Override
            public long getAmount() {
                return 0;
            }

            @Override
            public long getCapacity() {
                return 0;
            }
        };
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public StorageView<ItemVariant> next() {
                return null;
            }
        };
    }
}
