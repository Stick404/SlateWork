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
import org.sophia.slate_work.misc.DumbDumbInv;

import static org.sophia.slate_work.registries.BlockRegistry.CRAFTING_LOCI_ENTITY;

public class CraftingLociEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
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
}
