package org.sophia.slate_work.blocks.entities;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.items.storage.ItemFocus;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.registries.BlockRegistry;

public class MacroLociEntity extends BlockEntity implements Inventory {
    // The Holy Slot, The Slot. The Slot
    private ItemStack theSlot;
    public HexPattern pattern;

    public MacroLociEntity(BlockPos pos, BlockState state) {
        super(BlockRegistry.MACRO_LOCI_ENTITY, pos, state);
        // The Slot
        this.theSlot = ItemStack.EMPTY;
        this.pattern = HexPattern.fromAngles("aaeaadawawa", HexDir.EAST);
    }

    public HexPattern getPattern() {
        return pattern;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        // The Slot
        this.theSlot = ItemStack.fromNbt(nbt.getCompound("the_slot"));
        this.pattern = HexPattern.fromNBT(nbt.getCompound("pattern"));
    }

    public @Nullable Text getDisplay(){
        if (theSlot.getItem() instanceof ItemFocus item){
            if (item.readIotaTag(theSlot) != null) {
                return IotaType.getDisplay(item.readIotaTag(theSlot));
            }
        }
        return Text.of("");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtCompound compound = new NbtCompound();
        // The Slot
        theSlot.writeNbt(compound);
        nbt.put("pattern",this.pattern.serializeToNBT());
        nbt.put("the_slot",compound);
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
        return holder != null && stack.getItem() == HexItems.FOCUS.asItem();
    }

    @Override
    public ItemStack getStack(int slot) {
        return theSlot;
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
        this.markDirty();
        theSlot = ItemStack.EMPTY;
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
    public NbtCompound toInitialChunkDataNbt() {
        var compound = new NbtCompound();
        this.writeNbt(compound);
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
