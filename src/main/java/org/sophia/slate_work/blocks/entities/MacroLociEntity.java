package org.sophia.slate_work.blocks.entities;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.registries.SlateWorksBlockRegistry;

public class MacroLociEntity extends BlockEntity implements Inventory {
    // The Holy Slot, The Slot. The Slot
    private ItemStack theSlot;
    public HexPattern pattern;

    public MacroLociEntity(BlockPos pos, BlockState state) {
        super(SlateWorksBlockRegistry.MACRO_LOCI_ENTITY, pos, state);
        // The Slot
        this.theSlot = ItemStack.EMPTY;
        this.pattern = HexPattern.fromAngleString("qaq", HexDir.NORTH_EAST);
    }

    public HexPattern getPattern() {
        return pattern;
    }

    public void setFocusContents(Iota iota, @Nullable CastingEnvironment env) throws MishapOthersName {
        var holder = IXplatAbstractions.INSTANCE.findDataHolder(theSlot);
        if (this.isEmpty() || holder == null) return;
        var written = holder.writeIota(iota, false);
        if (written && env != null) {
            var trueName = MishapOthersName.getTrueNameMishapFromDatum(env.getWorld(), holder.readIota(), (ServerPlayerEntity) env.getCastingEntity());
            if (trueName != null)
                throw trueName;
        }

        this.markDirty();
    }

    public void setPattern(HexPattern pattern) {
        this.pattern = pattern;
        this.markDirty();
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        // The Slot
        this.theSlot = ItemStack.fromNbt(registryLookup, nbt.getCompound("the_slot")).get();
        this.pattern = HexPattern.CODEC.decode(NbtOps.INSTANCE, nbt.getCompound("pattern")).getOrThrow().getFirst();
    }

    public @Nullable Text getDisplay(){
        var holder = IXplatAbstractions.INSTANCE.findDataHolder(theSlot);
        if (holder != null){
            if (holder.readIota() != null) {
                return holder.readIota().display();
            }
        }
        return Text.of("");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        NbtCompound compound = new NbtCompound();
        // The Slot
        HexUtils.serializeToNBT(theSlot, registryLookup);
        nbt.put("pattern", HexPattern.CODEC.encodeStart(NbtOps.INSTANCE, this.pattern).getOrThrow());
        nbt.put("the_slot", compound);
    }

    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return false;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return theSlot.isEmpty();
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        ADIotaHolder holder = IXplatAbstractions.INSTANCE.findDataHolder(stack);
        return holder != null;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.theSlot.copy();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        var copy = theSlot.copy();
        theSlot = ItemStack.EMPTY;
        this.markDirty();
        return copy;
    }

    @Override
    public ItemStack removeStack(int slot) {
        var stack = this.theSlot.copy();
        theSlot = ItemStack.EMPTY;
        this.markDirty();
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        theSlot = stack;
        this.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var compound = new NbtCompound();
        this.writeNbt(compound, registryLookup);
        return compound;
    }

    @Override
    public void clear() {
        theSlot = ItemStack.EMPTY;
        this.markDirty();
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }
}
