package org.sophia.slate_work.client.lens;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.village.TradedItem;
import net.minecraft.world.World;
import org.sophia.slate_work.blocks.entities.TradeLociEntity;

import java.util.ArrayList;
import java.util.List;

public class TradeLociScrying implements ScryingLensOverlayRegistry.OverlayBuilder {
    @Override
    public void addLines(List<Pair<ItemStack, Text>> lines, BlockState state, BlockPos pos, PlayerEntity observer, World world, Direction hitFace) {
        if (world.getBlockEntity(pos) instanceof TradeLociEntity entity){
            lines.add(new Pair<>(new ItemStack(Items.VILLAGER_SPAWN_EGG), Text.translatable("entity.minecraft.villager." + entity.villagerData.getProfession().id())));

            int i = 0;
            for (var trade : entity.offerList){
                ItemStack first = trade.getOriginalFirstBuyItem().copy();
                ItemStack firstAdjusted = trade.getFirstBuyItem().itemStack().copy();
                ItemStack second = trade.getSecondBuyItem().orElseGet(() -> new TradedItem(ItemStack.EMPTY.getItem())).itemStack().copy();
                ItemStack sell = trade.getSellItem().copy();

                MutableText text = Text.empty();
                text.append(Text.literal(i + ": ").formatted(Formatting.GREEN));

                if (first.getCount() != firstAdjusted.getCount()) {
                    text.append(firstAdjusted.getName().copy().append("[item+" + firstAdjusted.getItem().toString() + "]"));
                    text.append(Text.literal("x" + first.getCount()).formatted(Formatting.RED, Formatting.STRIKETHROUGH));
                    text.append(Text.literal(" x" + firstAdjusted.getCount()));
                } else {
                    text.append(first.getName().copy().append("[item+" + first.getItem().toString() + "] x" + first.getCount()));
                }
                if (!second.isEmpty()) {
                    text.append(" ");
                    text.append(second.getName().copy().append("[item+" + second.getItem().toString() + "] x" + second.getCount()));
                }
                text.append(" → ");
                text.append(sell.getName().copy().append("[item+" + sell.getItem().toString() + "]"));

                ItemEnchantmentsComponent component = EnchantmentHelper.getEnchantments(sell);
                //NbtList nbtCheck = EnchantedBookItem.getEnchantmentNbt(sell);
                if (!component.isEmpty()) {

                    for (var enchant : component.getEnchantments()){
                        text.append(" ");
                        text.append(Enchantment.getName(enchant, component.getLevel(enchant)));
                    }
                }

                text.append(" x" + sell.getCount() + " [");
                text.append(Text.literal(trade.getUses() + " / " + trade.getMaxUses()).formatted(Formatting.AQUA));
                text.append("]");

                lines.add(new Pair<>(ItemStack.EMPTY, text));
                i++;
            }
        }
    }
}
