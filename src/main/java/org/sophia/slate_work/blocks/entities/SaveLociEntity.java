package org.sophia.slate_work.blocks.entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.sophia.slate_work.registries.BlockRegistry;

public class SaveLociEntity extends BlockEntity {
    public SaveLociEntity(BlockPos pos, BlockState state) {
        //super(BlockRegistry.SAVE_LOCI_ENTITY, pos, state);
        super(null, pos, state);
    }
}
