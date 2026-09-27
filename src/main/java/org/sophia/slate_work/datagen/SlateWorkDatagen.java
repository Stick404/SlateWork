package org.sophia.slate_work.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.block.Block;
import org.sophia.slate_work.registries.SlateWorksBlockRegistry;

import java.util.List;

public class SlateWorkDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(BlockModelDatagen::new);
        pack.addProvider(BlockLootTableDatagen::new);
        pack.addProvider(BlockTagDatagen::new);
    }

    public static final List<Block> BLOCKS = List.of(
            SlateWorksBlockRegistry.AMBIT_LOCI,
            SlateWorksBlockRegistry.MACRO_LOCI,
            SlateWorksBlockRegistry.CRAFTING_LOCI,
            SlateWorksBlockRegistry.SPEED_LOCI,
            SlateWorksBlockRegistry.STORAGE_LOCI,
            SlateWorksBlockRegistry.MUTE_LOCI,
            SlateWorksBlockRegistry.SENTINEL_LOCI,
            SlateWorksBlockRegistry.BROADCASTER_LOCI,
            SlateWorksBlockRegistry.LISTENING_IMPETUS,
            SlateWorksBlockRegistry.HOTBAR_LOCI,
            SlateWorksBlockRegistry.REDSTONE_LOCI,
            SlateWorksBlockRegistry.ACCELERATOR_LOCI,
            SlateWorksBlockRegistry.SAVE_LOCI,
            SlateWorksBlockRegistry.FAKE_PLAYER_LOCI,
            SlateWorksBlockRegistry.TRADE_LOCI,
            SlateWorksBlockRegistry.BLOCK_BREAKING_LOCI,

            SlateWorksBlockRegistry.SLATE_PLATED_EDIFIED_PLANKS,
            SlateWorksBlockRegistry.AMETHYST_EMBEDDED_SLATE,
            SlateWorksBlockRegistry.COPPER_PLATED_SLATE,
            SlateWorksBlockRegistry.REPLICATED_ALLAY
    );
}
