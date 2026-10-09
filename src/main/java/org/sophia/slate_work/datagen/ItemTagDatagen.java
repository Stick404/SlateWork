package org.sophia.slate_work.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.registries.SlateWorksBlockRegistry;

import java.util.concurrent.CompletableFuture;

import static org.sophia.slate_work.datagen.SlateWorkDatagen.BLOCKS;

public class ItemTagDatagen extends FabricTagProvider.ItemTagProvider {
    public ItemTagDatagen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(ItemTags.MINING_ENCHANTABLE).add(SlateWorksBlockRegistry.BLOCK_BREAKING_LOCI_ITEM);
        getOrCreateTagBuilder(ItemTags.DURABILITY_ENCHANTABLE).add(SlateWorksBlockRegistry.BLOCK_BREAKING_LOCI_ITEM);

        var enchants = getOrCreateTagBuilder(ItemTags.HEAD_ARMOR);
        for (Block block : BLOCKS){
            if (block instanceof Equipment e) {
                if (e.getSlotType() == EquipmentSlot.HEAD){
                    enchants.add(block.asItem());
                }
            }
        }
    }
}
