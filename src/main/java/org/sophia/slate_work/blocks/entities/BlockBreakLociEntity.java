package org.sophia.slate_work.blocks.entities;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import org.sophia.slate_work.registries.BlockRegistry;

import java.util.ArrayList;
import java.util.List;

public class BlockBreakLociEntity extends HexBlockEntity {
    private NbtList enchantments;
    private static final String TAG = "enchantments";

    public BlockBreakLociEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockRegistry.BLOCK_BREAK_LOCI_ENTITY, pWorldPosition, pBlockState);
        this.enchantments = new NbtList();
    }

    @Override
    protected void saveModData(NbtCompound tag) {
        tag.put(TAG, this.enchantments);
    }

    @Override
    protected void loadModData(NbtCompound tag) {
        this.enchantments = tag.getList(TAG, NbtElement.COMPOUND_TYPE);
    }

    public NbtList getEnchantments() {
        return enchantments;
    }

    public void setEnchantments(NbtList enchantments) {
        this.enchantments = enchantments;
    }
}
