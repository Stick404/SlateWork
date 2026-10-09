package org.sophia.slate_work.client.lens;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.sophia.slate_work.blocks.entities.StorageLociEntity;
import org.sophia.slate_work.storage.StorageLociSlot;

import java.util.List;

public class StorageLociScrying implements ScryingLensOverlayRegistry.OverlayBuilder{
    @Override
    public void addLines(List<Pair<ItemStack, Text>> list, BlockState blockState, BlockPos blockPos, PlayerEntity playerEntity, World world, Direction direction) {
        var entity = world.getBlockEntity(blockPos);
        if (entity instanceof StorageLociEntity loci){
            for (StorageLociSlot z : loci.getInventory()){
                if (z.isResourceBlank())
                    continue;
                ItemVariant var = z.getResource();
                var name = var.getItem().getName(var.toStack()).copy();

                list.add(new Pair<>(
                        var.toStack(),
                        name.append(Text.literal(" x").append(String.valueOf(z.getAmount())))
                    )
                );
            }
        }
    }
}
