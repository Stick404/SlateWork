package org.sophia.slate_work.client.blockEntityRenders;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.sophia.slate_work.blocks.entities.HotbarLociEntity;
import org.sophia.slate_work.registries.BlockRegistry;

public class HotbarLocirenderer implements BlockEntityRenderer<HotbarLociEntity> {
    private final ItemRenderer itemRenderer;

    public HotbarLocirenderer(BlockEntityRendererFactory.Context context){
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(HotbarLociEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        if (entity.getWorld() != null) {
            var bs = entity.getWorld().getBlockState(entity.getPos());
            if (bs.getBlock() == BlockRegistry.HOTBAR_LOCI && MinecraftClient.getInstance().getCameraEntity() != null) {

            }
        }
        matrices.pop();
    }
}
