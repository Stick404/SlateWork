package org.sophia.slate_work.client.lens;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
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

import java.util.List;

import static org.sophia.slate_work.Slate_work.IS_LEFT_CLICKING;
import static org.sophia.slate_work.Slate_work.IS_OPTIONAL_VECTOR;

public class FakePlayerLociScrying implements ScryingLensOverlayRegistry.OverlayBuilder{
    @Override
    public void addLines(List<Pair<ItemStack, Text>> lines, BlockState state, BlockPos pos, PlayerEntity observer, World world, Direction hitFace) {
        Text isOptional;
        if (state.get(IS_OPTIONAL_VECTOR)) {
            isOptional = Text.translatable("hexcasting.tooltip.boolean_true").formatted(Formatting.DARK_GREEN);
        } else {
            isOptional = Text.translatable("hexcasting.tooltip.boolean_false").formatted(Formatting.DARK_RED);
        }
        Text isLeftClick;
        if (!state.get(IS_LEFT_CLICKING)) {
            isLeftClick = Text.translatable("hexcasting.tooltip.boolean_true").formatted(Formatting.DARK_GREEN);
        } else {
            isLeftClick = Text.translatable("hexcasting.tooltip.boolean_false").formatted(Formatting.DARK_RED);
        }
        lines.add(new Pair<>(
                Items.ENDER_PEARL.getDefaultStack(),
                Text.translatable("slate_work.scrying.fake_player").append(isOptional)
        ));
        lines.add(new Pair<>(
                Items.DIAMOND_SWORD.getDefaultStack(),
                Text.translatable("slate_work.scrying.fake_player.2").append(isLeftClick)
        ));
    }
}
