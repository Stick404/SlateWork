package org.sophia.slate_work.client.blockEntityRenders;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.sophia.slate_work.blocks.entities.HotbarLociEntity;
import org.sophia.slate_work.registries.BlockRegistry;

import java.util.ArrayList;

import static at.petrak.hexcasting.api.block.circle.BlockCircleComponent.ENERGIZED;
import static org.sophia.slate_work.blocks.AbstractSlate.FACING;
import static org.sophia.slate_work.client.blockEntityRenders.SaveLociRenderer.rotateY;

public class HotbarLociRenderer implements BlockEntityRenderer<HotbarLociEntity> {
    private final ItemRenderer itemRenderer;

    public HotbarLociRenderer(BlockEntityRendererFactory.@NotNull Context context){
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public boolean isInRenderDistance(HotbarLociEntity blockEntity, Vec3d pos) {
        return BlockEntityRenderer.super.isInRenderDistance(blockEntity, pos);
    }

    @Override
    public int getRenderDistance() {
        return BlockEntityRenderer.super.getRenderDistance();
    }

    @Override
    public void render(HotbarLociEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5,0.5,0.5);
        if (entity.getWorld() != null) {
            var bs = entity.getWorld().getBlockState(entity.getPos());
            if (bs.getBlock() == BlockRegistry.HOTBAR_LOCI && MinecraftClient.getInstance().getCameraEntity() != null) {
                double speed = 3;
                if (bs.get(ENERGIZED)) speed = 8;
                long time = entity.getWorld().getTime();

                int rotationX = 0;
                int rotationY = 0;
                switch (bs.get(FACING)){
                    case UP -> rotationY = 0;
                    case DOWN -> rotationY = 180;
                    case NORTH -> rotationY = 270;
                    case SOUTH -> rotationY = 90;
                    case EAST -> {rotationY = 90; rotationX = 270;}
                    case WEST -> {rotationY = 270; rotationX = 90;}
                }


                float radZ = (float) (Math.PI/180)*rotationY;
                matrices.multiply(new Quaternionf(Math.sin(radZ/2),0,0f,Math.cos(radZ/2f)), 0f, 0, 0f);

                float radX = (float) (Math.PI/180)*rotationX;
                matrices.multiply(new Quaternionf(0,0,Math.sin(radX/2),Math.cos(radX/2f)));

                matrices.push();
                matrices.scale(1,1,1);
                matrices.translate(0,-0.7,0);

                double spin = (Math.PI/180)*time%360*speed;
                matrices.multiply(new Quaternionf(0,Math.sin(spin/2),0,Math.cos(spin/2f)));

                var itemsTemp = entity.getStacks();
                var items = new ArrayList<ItemStack>();
                for (var item : itemsTemp){
                    if (!item.isEmpty()) items.add(item);
                }
                ItemStack selectedSlot = entity.getCurrentSlot();
                var radius = (Math.PI*2)/items.toArray().length;
                int i = 0;
                float scale = (float) (1/radius)-0.15f;
                matrices.scale(0.75f,0.75f,0.75f);
                for (ItemStack item : items){
                    matrices.push();
                    matrices.translate(Math.sin(radius*i)*scale,
                            Math.sin((double) time/10)/(6+4/speed) +1, //Math.sin((time/100)/(speed/4)) +1
                            Math.cos(radius*i)*scale);
                    if (item == selectedSlot){
                        matrices.translate(0,0.2,0);
                        matrices.scale(1.2f,1.2f,1.2f);
                    }
                    BakedModel bakedModel = itemRenderer.getModel(item, entity.getWorld(), null, 0);
                    itemRenderer.renderItem(item, ModelTransformationMode.GROUND, false, matrices, vertexConsumers, light, overlay, bakedModel);
                    matrices.pop();
                    i++;
                }
                matrices.pop();
            }
        }
        matrices.pop();
    }
}
