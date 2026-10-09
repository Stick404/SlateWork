package org.sophia.slate_work.blocks.entities;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.sophia.slate_work.registries.SlateWorksBlockRegistry;

public class BlockBreakLociEntity extends HexBlockEntity {
    private ItemEnchantmentsComponent enchantments;
    private static final String TAG = "enchantments";

    public BlockBreakLociEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(SlateWorksBlockRegistry.BLOCK_BREAK_LOCI_ENTITY, pWorldPosition, pBlockState);
        this.enchantments = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT).build();
    }

    @Override
    protected void saveModData(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        tag.put(TAG, ItemEnchantmentsComponent.CODEC.encodeStart(NbtOps.INSTANCE, this.enchantments).getOrThrow());
    }

    @Override
    protected void loadModData(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        this.enchantments = ItemEnchantmentsComponent.CODEC.decode(NbtOps.INSTANCE, tag.getCompound(TAG)).getOrThrow().getFirst();
    }

    public ItemEnchantmentsComponent getEnchantments() {
        return enchantments;
    }

    public void setEnchantments(ItemEnchantmentsComponent enchantments) {
        this.enchantments = enchantments;
    }
}
