package org.sophia.slate_work.client.blockEntityRenders;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.sophia.slate_work.blocks.entities.MacroLociEntity;
import org.sophia.slate_work.registries.BlockRegistry;

import static at.petrak.hexcasting.api.block.circle.BlockCircleComponent.ENERGIZED;
import static org.sophia.slate_work.blocks.AbstractSlate.FACING;

public class MacroLociRenderer implements BlockEntityRenderer<MacroLociEntity> {
    private final ItemRenderer itemRenderer;

    public MacroLociRenderer(BlockEntityRendererFactory.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(MacroLociEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        if (entity.getWorld() != null) {
            var bs = entity.getWorld().getBlockState(entity.getPos());
            if (bs.getBlock() == BlockRegistry.MACRO_LOCI && MinecraftClient.getInstance().getCameraEntity() != null) {
                var camEntity = MinecraftClient.getInstance().getCameraEntity();

                matrices.translate(0.5, 0.5, 0.5);
                float rad = (float) (Math.PI/180)*-camEntity.getYaw(tickDelta);
                matrices.multiply(new Quaternionf(0,Math.sin(rad/2),0,Math.cos(rad/2)),0,0,0);
                matrices.multiply(bs.get(FACING).getRotationQuaternion());

                int amount;
                int hight;
                if (bs.get(ENERGIZED)) {
                    amount = 5;
                    hight = 10;
                }else {
                    amount = 10;
                    hight = 25;
                }

                matrices.translate(0.0, -0.15+Math.sin((double) entity.getWorld().getTime()/amount)/hight, 0.0);
                matrices.scale(1.25f, 1.25f, 1.25f);
                var stack = entity.getStack(0);

                BakedModel bakedModel = itemRenderer.getModel(stack, entity.getWorld(), null, 0);
                itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, false, matrices, vertexConsumers, light, overlay, bakedModel);
            }
        }
        matrices.pop();
    }
}
