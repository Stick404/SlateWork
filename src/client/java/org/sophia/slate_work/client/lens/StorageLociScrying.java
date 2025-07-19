package org.sophia.slate_work.client.lens;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.sophia.slate_work.blocks.entities.StorageLociEntity;

import java.util.List;

public class StorageLociScrying implements ScryingLensOverlayRegistry.OverlayBuilder{
    @Override
    public void addLines(List<Pair<ItemStack, Text>> list, BlockState blockState, BlockPos blockPos, PlayerEntity playerEntity, World world, Direction direction) {
        var entity = world.getBlockEntity(blockPos);
        if (entity instanceof StorageLociEntity loci){
            for (var z : loci.getInventory()){
                var name = z.getLeft().getItem().getName(z.getLeft().toStack()).copy();
                if (z.getLeft().isBlank())
                    continue;
                list.add(new Pair<>(
                        z.getLeft().toStack(),
                        name.append(Text.literal(" x").append(z.getRight().toString()))
                        )
                );
            }
        }
    }
}
