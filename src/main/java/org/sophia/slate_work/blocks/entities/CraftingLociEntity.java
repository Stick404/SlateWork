package org.sophia.slate_work.blocks.entities;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.GUI.Ghost3x3ScreenHandler;

import static org.sophia.slate_work.registries.BlockRegistry.CRAFTING_LOCI_ENTITY;

public class CraftingLociEntity extends BlockEntity implements ExtendedScreenHandlerFactory, Inventory {
    DefaultedList<ItemStack> inv = DefaultedList.ofSize(10,ItemStack.EMPTY);

    public CraftingLociEntity(BlockPos pos, BlockState state) {
        super(CRAFTING_LOCI_ENTITY, pos, state);
    }

    public DefaultedList<ItemStack> getInv() {
        return inv;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.slate_work.crafting_slate");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new Ghost3x3ScreenHandler(syncId, playerInventory, this);
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

    @Override
    public int size() {
        return 10;
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false; // Hmm, might be wrong...
    }

    @Override
    public ItemStack getStack(int slot) {
        return inv.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return this.removeStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot) {
        this.inv.set(slot, ItemStack.EMPTY);
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        stack = stack.copy();
        stack.setCount(1);
        this.inv.set(slot,stack);
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.inv = DefaultedList.ofSize(10,ItemStack.EMPTY);
        this.markDirty();
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }
}
