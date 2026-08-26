package org.sophia.slate_work.item;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.sophia.slate_work.registries.BlockRegistry;

public class BlockBreakLociItem extends BlockItem {
    public static final ThreadLocal<Object> CALLING_DAMAGEABLE_FROM_ANVIL = ThreadLocal.withInitial(() -> null);

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


    @Override
    public int getMaxDamage() {
        return CALLING_DAMAGEABLE_FROM_ANVIL.get() != null ? 1 : super.getMaxDamage();
    }
}
