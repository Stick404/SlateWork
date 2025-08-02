package org.sophia.slate_work.client.lens;

import at.petrak.hexcasting.api.casting.iota.IotaType;
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
import org.sophia.slate_work.blocks.entities.BroadcasterLociEntity;

import java.util.List;

public class BroadcasterLociScrying implements ScryingLensOverlayRegistry.OverlayBuilder{
    @Override
    public void addLines(List<Pair<ItemStack, Text>> list, BlockState blockState, BlockPos blockPos, PlayerEntity playerEntity, World world, Direction direction) {
        if (world.getBlockEntity(blockPos) instanceof BroadcasterLociEntity entity){
            list.add(new Pair<>(
                    Items.SCULK_SHRIEKER.getDefaultStack(), IotaType.getDisplay(entity.getIotaCompound())
            ));
        }
    }
}
