package org.sophia.slate_work.blocks.entities;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.sophia.slate_work.GUI.FakeInvHotbarLoci;
import org.sophia.slate_work.GUI.HotbarLociScreenHandler;
import org.sophia.slate_work.storage.HotbarLociSlot;
import org.sophia.slate_work.storage.LociIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.sophia.slate_work.registries.BlockRegistry.HOTBAR_LOCI_ENTITY;

@SuppressWarnings("UnstableApiUsage")
public class HotbarLociEntity extends HexBlockEntity implements SlottedStorage<ItemVariant>, ExtendedScreenHandlerFactory {
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
        if (world != null && world.isClient) stacks.clear(); // Done only on the client as a basic protection
        Inventories.readNbt(tag, stacks);
        slot = tag.getInt("select");
    }

    public ItemStack removeStack(int slot){
        var stack = this.stacks.get(slot).copy();
        this.stacks.set(slot, ItemStack.EMPTY);
        this.sync();
        return stack;
    }

    public ItemStack removeStack(int slot, int count){
        var stack = this.stacks.get(slot);
        this.sync();
        return stack.split(count);
    }

    public List<ItemStack> getStacks(){
        return this.stacks;
    }
    public List<ItemStack> getStacksSorted(){
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < 5; i++){
            list.add(this.getSlotStack(Math.floorMod(this.getSlot()+i, 5)));
        }
        return list;
    }

    public ItemStack getCurrentSlot(){
        return this.stacks.get(this.getSlot());
    }

    public int getSlot() {
        return Math.floorMod(slot, 5); // In case someone fucks shit up (me)
    }

    public void setSlot(int slot) {
        this.slot = slot;
        this.sync();
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

    public void setStack(int slot, ItemStack stack){
        this.stacks.set(slot, stack);
        this.sync();
    }

    @Override
    public void sync() {
        super.sync();
        if (this.world instanceof ServerWorld world){
            world.getChunkManager().markForUpdate(this.getPos());
        }
    }

    public Inventory getInv(){
        return new FakeInvHotbarLoci(this);
    }

    @Override
    public @UnmodifiableView List<SingleSlotStorage<ItemVariant>> getSlots() {
        ArrayList<SingleSlotStorage<ItemVariant>> slots = new ArrayList<>();
        int i = 0;
        for (var item : this.stacks) {
            slots.add(new HotbarLociSlot(this, i));
            i++;
        }
        return slots;
    }

    @Override
    public int getSlotCount() {
        return 6;
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        return new HotbarLociSlot(this, slot);
    }

    public int getFreeSlot(ItemVariant variant){
        int i = 0;
        for (var item : this.stacks){
            if ((ItemVariant.of(item) == variant || item.isEmpty()) && item.getMaxCount() > item.getCount()) return i;
            i++;
        }
        return -1;
    }

    public ItemStack getSlotStack(int slot){
        return this.stacks.get(slot);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        var slot = getFreeSlot(resource);
        if (slot == -1) return 0;

        var stack = getSlotStack(slot);
        var copy = stack.copy();
        if (copy.isEmpty()) {
            this.setStack(slot, resource.toStack((int) maxAmount));
            this.sync();
        }

        if (copy.getCount()+maxAmount > copy.getMaxCount()) {
            stack.setCount(copy.getMaxCount());
            this.sync();
            return maxAmount-copy.getCount();
        } else {
            stack.setCount(copy.getCount() + (int) maxAmount);
            this.sync();
            return maxAmount;
        }
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        transaction.addCloseCallback((a,b) -> {
            if (b.wasCommitted()) {
                int i = 0;
                for (var stack : this.stacks){
                    if (ItemVariant.of(stack).equals(resource)) break;
                    i++;
                }
                this.getStacks().get(i).split((int) maxAmount);
                this.sync();
            }
        });
        ItemStack selected = null;
        for (var stack : this.stacks){
            if (ItemVariant.of(stack).equals(resource)){
                selected = stack;
                break;
            }
        }
        if (selected == null) return 0;
        int count = selected.getCount();
        if (count > maxAmount){
            return maxAmount;
        } else {
            return count;
        }
    }

    @Override
    public @NotNull Iterator<StorageView<ItemVariant>> iterator() {
        return new LociIterator<>(this);
    }
}
