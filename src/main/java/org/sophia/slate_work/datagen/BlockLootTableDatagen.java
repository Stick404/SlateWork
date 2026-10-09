package org.sophia.slate_work.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryWrapper;
import org.sophia.slate_work.blocks.BlockBreakLoci;
import org.sophia.slate_work.blocks.StorageLoci;
import org.sophia.slate_work.blocks.TradeLoci;

import java.util.concurrent.CompletableFuture;

import static org.sophia.slate_work.datagen.SlateWorkDatagen.BLOCKS;

public class BlockLootTableDatagen extends FabricBlockLootTableProvider {

    protected BlockLootTableDatagen(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        for (Block block : BLOCKS){
            if (block instanceof StorageLoci || block instanceof TradeLoci || block instanceof BlockBreakLoci) continue;
            this.addDrop(block);
        }
    }
}
