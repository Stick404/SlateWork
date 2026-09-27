package org.sophia.slate_work.item;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.sophia.slate_work.registries.BlockRegistry;

public class BlockBreakLociItem extends BlockItem {
    public BlockBreakLociItem(Settings settings) {
        super(BlockRegistry.BLOCK_BREAKING_LOCI, settings);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantability() {
        return 15;
    }

}
