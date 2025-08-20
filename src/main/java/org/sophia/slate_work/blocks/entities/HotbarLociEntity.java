package org.sophia.slate_work.blocks.entities;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
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
import org.sophia.slate_work.GUI.HotbarLociScreenHandler;

import java.util.List;

import static org.sophia.slate_work.registries.BlockRegistry.HOTBAR_LOCI_ENTITY;

public class HotbarLociEntity extends HexBlockEntity implements Inventory, ExtendedScreenHandlerFactory {
    private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(6, ItemStack.EMPTY);
    private int slot = 0;

    public HotbarLociEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(HOTBAR_LOCI_ENTITY, pWorldPosition, pBlockState);
    }

    @Override
    protected void saveModData(NbtCompound tag) {
        Inventories.writeNbt(tag, stacks);
        tag.putInt("select", slot);
    }

    @Override
    protected void loadModData(NbtCompound tag) {
        Inventories.readNbt(tag, stacks);
        slot = tag.getInt("select");
    }

    @Override
    public int size() {
        return 6;
    }

    public List<ItemStack> getStacks(){
        return this.stacks;
    }

    public ItemStack getCurrentSlot(){
        return this.stacks.get(this.slot);
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    @Override
    public boolean isEmpty() {
        boolean notEmpty = false;
        for (var z :stacks){
            notEmpty = notEmpty || !z.isEmpty();
        }
        return notEmpty;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (!world.isClient) this.sync();
        return this.stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return this.stacks.get(slot).split(amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        var stack = this.stacks.get(slot).split(64);
        this.sync();
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.stacks.clear();
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.slate_work.hotbar_loci");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new HotbarLociScreenHandler(syncId, playerInventory, this);
    }
}
