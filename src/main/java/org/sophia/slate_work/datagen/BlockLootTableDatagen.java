package org.sophia.slate_work.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import org.sophia.slate_work.blocks.StorageLoci;

import static org.sophia.slate_work.datagen.SlateWorkDatagen.BLOCKS;

public class BlockLootTableDatagen extends FabricBlockLootTableProvider {

    protected BlockLootTableDatagen(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generate() {
        for (Block block : BLOCKS){
            if (block instanceof StorageLoci) continue;
            this.addDrop(block);
        }
    }
}
