package org.sophia.slate_work.client.lens;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.sophia.slate_work.blocks.RedstoneLoci;
import org.sophia.slate_work.registries.SlateWorksBlockRegistry;

import java.util.List;

public class RedstoneLociScrying implements ScryingLensOverlayRegistry.OverlayBuilder {
    @Override
    public void addLines(List<Pair<ItemStack, Text>> lines, BlockState state, BlockPos pos, PlayerEntity observer, World world, Direction hitFace) {
        // I think this is already known, but *just* to double-check
        if (state.isOf(SlateWorksBlockRegistry.REDSTONE_LOCI)){
            boolean receive = state.get(RedstoneLoci.RECEIVE);
            if (receive) {
                lines.add(new Pair<>(Items.REPEATER.getDefaultStack(), Text.translatable("slate_work.scrying.redstone.receive_on")));
            } else {
                lines.add(new Pair<>(Items.REPEATER.getDefaultStack(), Text.translatable("slate_work.scrying.redstone.receive_off")));
            }
        }
    }
}
