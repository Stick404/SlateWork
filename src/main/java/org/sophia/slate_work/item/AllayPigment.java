package org.sophia.slate_work.item;

import at.petrak.hexcasting.api.addldata.ADPigment;
import at.petrak.hexcasting.api.item.PigmentItem;
import at.petrak.hexcasting.api.pigment.ColorProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class AllayPigment extends Item implements PigmentItem {
    public AllayPigment(Settings settings) {
        super(settings);
    }

    @Override
    public ColorProvider provideColor(ItemStack itemStack, UUID uuid) {
        return colorProvider;
    }

    protected MyColorProvided colorProvider = new MyColorProvided();

    protected static class MyColorProvided extends ColorProvider{
        @Override
        protected int getRawColor(float v, Vec3d vec3d) {
            return ADPigment.morphBetweenColors(new int[]{
                    0xFF6fe4d3, 0xFF2db6d0, //Blue
                    0xFFcfa0f3, 0xFFfecbe6 // Amethyst
            },new Vec3d(0.1,0.1,0.1), v / 70, vec3d);
        }
    }
}
