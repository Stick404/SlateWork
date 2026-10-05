package org.sophia.slate_work.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.util.math.BlockPos;

/// This is a smaller helper class that holds Codecs for Kotlin classes.
/// Mostly because I do not want to learn how to do the functional
public class SlateWorksCodecs {
    private SlateWorksCodecs() {};

    public static final Codec<CircleHelper.ItemSlot> ITEM_SLOT_CODEC  = RecordCodecBuilder.create (
            instance -> instance.group(
                    ItemVariant.CODEC.fieldOf("item").forGetter(CircleHelper.ItemSlot::getItem),
                    PrimitiveCodec.LONG.fieldOf("count").forGetter(CircleHelper.ItemSlot::getCount),
                    BlockPos.CODEC.fieldOf("pos").forGetter(CircleHelper.ItemSlot::getPos)
            ).apply(instance, CircleHelper.ItemSlot::new));
}
