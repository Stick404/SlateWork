package org.sophia.slate_work.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import org.sophia.slate_work.blocks.BlockBreakLoci;
import org.sophia.slate_work.registries.SlateWorksBlockRegistry;

import java.util.concurrent.CompletableFuture;

import static org.sophia.slate_work.datagen.SlateWorkDatagen.BLOCKS;

public class ItemTagDatagen extends FabricTagProvider.ItemTagProvider {
    public ItemTagDatagen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(ItemTags.MINING_ENCHANTABLE)
                .add(SlateWorksBlockRegistry.BLOCK_BREAKING_LOCI_ITEM);
        getOrCreateTagBuilder(ItemTags.DURABILITY_ENCHANTABLE)
                .add(SlateWorksBlockRegistry.BLOCK_BREAKING_LOCI_ITEM);
        getOrCreateTagBuilder(ItemTags.MINING_LOOT_ENCHANTABLE)
                .add(SlateWorksBlockRegistry.BLOCK_BREAKING_LOCI_ITEM);

        var headArmor = getOrCreateTagBuilder(ItemTags.HEAD_ARMOR);
        var headEnchant = getOrCreateTagBuilder(ItemTags.HEAD_ARMOR_ENCHANTABLE);
        var wearableEnchant = getOrCreateTagBuilder(ItemTags.EQUIPPABLE_ENCHANTABLE);

        for (Block block : BLOCKS){
            if (block instanceof Equipment e && !(block instanceof BlockBreakLoci)) {
                if (e.getSlotType() == EquipmentSlot.HEAD){
                    headArmor.add(block.asItem());
                    headEnchant.add(block.asItem());
                    wearableEnchant.add(block.asItem());
                }
            }
        }
    }
}
