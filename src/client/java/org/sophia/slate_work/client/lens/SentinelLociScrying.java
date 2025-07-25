package org.sophia.slate_work.client.lens;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.sophia.slate_work.blocks.entities.MacroLociEntity;
import org.sophia.slate_work.blocks.entities.SentinelLociEntity;

import java.util.List;

public class SentinelLociScrying implements ScryingLensOverlayRegistry.OverlayBuilder{
    @Override
    public void addLines(List<Pair<ItemStack, Text>> list, BlockState blockState, BlockPos blockPos, PlayerEntity playerEntity, World world, Direction direction) {
        var loci = world.getBlockEntity(blockPos);
        if (loci instanceof SentinelLociEntity sent){
            var vec = sent.getSentPos();
            list.add(new Pair<>(
                    Items.ENDER_EYE.getDefaultStack(),
                    HexUtils.styledWith(Text.literal(String.format("(%.2f, %.2f, %.2f)", vec.x, vec.y, vec.z)), Formatting.RED)
                    ));
        }
    }
}
