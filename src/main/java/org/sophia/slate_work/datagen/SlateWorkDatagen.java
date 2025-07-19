package org.sophia.slate_work.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.block.Block;
import org.sophia.slate_work.registries.BlockRegistry;

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
            BlockRegistry.AMBIT_LOCI,
            BlockRegistry.MACRO_LOCI,
            BlockRegistry.CRAFTING_LOCI,
            BlockRegistry.SPEED_LOCI,
            BlockRegistry.STORAGE_LOCI
    );
}
