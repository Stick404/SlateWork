package org.sophia.slate_work.client.lens;

import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.sophia.slate_work.blocks.entities.MacroLociEntity;

import java.util.List;

public class MacroLociScrying implements ScryingLensOverlayRegistry.OverlayBuilder {
    @Override
    public void addLines(List<Pair<ItemStack, Text>> list, BlockState blockState, BlockPos blockPos, PlayerEntity playerEntity, World world, Direction direction) {
        var loci = world.getBlockEntity(blockPos);
        if (!(loci instanceof MacroLociEntity)){
            return;
        }
        list.add(new Pair<>(ItemStack.EMPTY,
                HexUtils.styledWith(Text.translatable(
                    "slate_work.scrying.macro.top"), Formatting.GOLD)
                .append(new PatternIota(((MacroLociEntity) loci).getPattern()).display())));
        var item = ((MacroLociEntity) loci).getStack(0);
        try {
            if (item.getItem() == HexItems.FOCUS) {
                list.add(new Pair<>(item,
                        ((MacroLociEntity) loci).getDisplay()
                ));
            }
        } catch (Exception e) {
            list.add(new Pair<>(HexItems.LORE_FRAGMENT.getDefaultStack(), HexUtils.styledWith(Text.translatable("slate_work.scrying.macro.error"), Formatting.DARK_RED)));
        }
    }
}
