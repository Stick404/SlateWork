package org.sophia.slate_work.client.blockEntityRenders;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import org.sophia.slate_work.blocks.entities.MacroLociEntity;
import org.sophia.slate_work.registries.BlockRegistry;

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
            if (bs.getBlock() == BlockRegistry.MACRO_LOCI) {
                matrices.translate(0.5, 0.5, 0.5);
                matrices.multiply(bs.get(FACING).getRotationQuaternion());
                matrices.translate(0.0, -0.15, 0.0);
                matrices.scale(1.25f, 1.25f, 1.25f);
                var stack = entity.getStack(0);

                BakedModel bakedModel = itemRenderer.getModel(stack, entity.getWorld(), null, 0);
                itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, false, matrices, vertexConsumers, light, overlay, bakedModel);
            }
        }
        matrices.pop();
    }
}
