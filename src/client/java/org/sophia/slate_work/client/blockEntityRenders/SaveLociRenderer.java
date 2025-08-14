package org.sophia.slate_work.client.blockEntityRenders;

import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;
import org.sophia.slate_work.blocks.entities.SaveLociEntity;

import static org.sophia.slate_work.Slate_work.MOD_ID;
import static org.sophia.slate_work.blocks.SaveLoci.HORIZONTAL;

public class SaveLociRenderer implements BlockEntityRenderer<SaveLociEntity> {
    public SaveLociRenderer(BlockEntityRendererFactory.Context context){
    }

    @Override
    public void render(SaveLociEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getWorld() != null) {
            var bs = entity.getWorld().getBlockState(entity.getPos());
            if (bs.isAir()) return;
            if (!bs.contains(HORIZONTAL)) return;
            matrices.push();
            int rotation = 0;
            float rad = (float) (Math.PI/180)*(entity.getWorld().getTime()*8);
            var buf = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(new Identifier(MOD_ID,"textures/white_texture.png")));
            switch (bs.get(HORIZONTAL)){
                case EAST -> rotation = 270;
                case SOUTH -> rotation = 180;
                case WEST -> rotation = 90;
            }
            rotateY(matrices, rotation);

            float scale = 0.20f;

            matrices.translate(0,1,0.2);
            matrices.scale(scale,scale,scale);

            matrices.push();
            matrices.multiply(new Quaternionf(0,0,Math.cos(rad/2),Math.sin(rad/2f)),0.25f*(1/scale)+0.5f,0.45f*(1/scale)+0.5f,0);
            matrices.translate(0.25*(1/scale),0.45*(1/scale),0); //Right
            makeSquare(buf,0x99c890f0, matrices);
            matrices.pop();
            matrices.push();
            matrices.multiply(new Quaternionf(0,0,Math.cos(rad/2),Math.sin(rad/2f)),0.55f*(1/scale)+0.5f,0.45f*(1/scale)+0.5f,0);
            matrices.translate(0.55*(1/scale),0.45*(1/scale),0); //Left
            makeSquare(buf,0x99c890f0, matrices);
            matrices.pop();


            matrices.push();
            matrices.translate(0,0,0.1);
            rad = rad + (float) Math.PI;
            matrices.push();
            matrices.multiply(new Quaternionf(0,0,Math.cos(rad/2),Math.sin(rad/2f)),0.25f*(1/scale)+0.5f,0.45f*(1/scale)+0.5f,0);
            matrices.translate(0.25*(1/scale),0.45*(1/scale),0); //Right
            makeSquare(buf,0x99c890f0, matrices);
            matrices.pop();

            matrices.push();
            matrices.multiply(new Quaternionf(0,0,Math.cos(rad/2),Math.sin(rad/2f)),0.55f*(1/scale)+0.5f,0.45f*(1/scale)+0.5f,0);
            matrices.translate(0.55*(1/scale),0.45*(1/scale),0); //Left
            makeSquare(buf,0x99c890f0, matrices);
            matrices.pop();

            matrices.pop();
            matrices.pop();
        }
    }

    public static void rotateY(MatrixStack matrices, int y){
        float rad = (float) (Math.PI/180)*y;
        matrices.multiply(new Quaternionf(0,Math.sin(rad/2),0f,Math.cos(rad/2f)),0.5f,0.5f,0.5f);
    }

    public static void makeSquare(VertexConsumer buff, int color, MatrixStack stack){
        var pose = stack.peek();
        var view = pose.getPositionMatrix();
        var normalMatrix = pose.getNormalMatrix();
        buff.vertex(view, 0f, 1f, 0f).color(color).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
        buff.vertex(view, 1f, 1f, 0f).color(color).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
        buff.vertex(view, 1f, 0f, 0f).color(color).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
        buff.vertex(view, 0f, 0f, 0f).color(color).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
    }
}
