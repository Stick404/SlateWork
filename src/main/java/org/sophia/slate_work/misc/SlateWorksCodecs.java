package org.sophia.slate_work.misc;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.utils.TreeList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kotlin.jvm.optionals.OptionalsKt;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import org.sophia.slate_work.casting.contuinations.FrameBreakBlockLoci;
import org.sophia.slate_work.casting.contuinations.FrameCheckItems;
import org.sophia.slate_work.casting.contuinations.FrameGetItems;
import org.sophia.slate_work.casting.contuinations.JankyMaybe;
import ram.talia.moreiotas.api.casting.iota.ItemStackIota;

import java.util.ArrayList;
import java.util.Optional;

/// This is a smaller helper class that holds Codecs for Kotlin classes.
/// Mostly because I do not want to learn how to do the functional
public class SlateWorksCodecs {
    private SlateWorksCodecs() {};

    public static final Codec<CircleHelper.ItemSlot> ITEM_SLOT_CODEC  = RecordCodecBuilder.create(
            instance -> instance.group(
                    ItemVariant.CODEC.fieldOf("item").forGetter(CircleHelper.ItemSlot::getItem),
                    PrimitiveCodec.LONG.fieldOf("count").forGetter(CircleHelper.ItemSlot::getCount),
                    BlockPos.CODEC.fieldOf("pos").forGetter(CircleHelper.ItemSlot::getPos)
            ).apply(instance, CircleHelper.ItemSlot::new));

    public static final PacketCodec<RegistryByteBuf, CircleHelper.ItemSlot> ITEM_SLOT_PACKET = PacketCodec.tuple(
            ItemVariant.PACKET_CODEC, CircleHelper.ItemSlot::getItem,
            PacketCodecs.VAR_LONG, CircleHelper.ItemSlot::getCount,
            BlockPos.PACKET_CODEC, CircleHelper.ItemSlot::getPos,
            CircleHelper.ItemSlot::new
    );

    public static final MapCodec<FrameGetItems> FRAME_GET_ITEMS_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("code").forGetter(FrameGetItems::getCode),
            TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("baseStack").forGetter(FrameGetItems::getBaseStack),
            ITEM_SLOT_CODEC.listOf().fieldOf("toCheck").forGetter(FrameGetItems::getToCheck),
            ITEM_SLOT_CODEC.optionalFieldOf("oldReturn").forGetter(a -> {
                var ret = a.getOldReturn();
                if (ret == null) {
                    return Optional.empty();
                } else {
                    return Optional.of(ret);
                }
            }),
            Codecs.ESCAPED_STRING.fieldOf("jankyMaybe").forGetter(a -> a.isFirst().name())
    ).apply(instance, (code, baseStack, toCheck, oldReturn, jankyMaybe) ->
            new FrameGetItems(code, baseStack, toCheck, OptionalsKt.getOrNull(oldReturn), JankyMaybe.valueOf(JankyMaybe.class, jankyMaybe))));

    public static final PacketCodec<RegistryByteBuf, FrameGetItems> FRAME_GET_ITEMS_PACKET_CODEC = PacketCodec.tuple(
            TreeList.<RegistryByteBuf, Iota>streamCodecOp().apply(IotaType.TYPED_STREAM_CODEC), FrameGetItems::getCode,
            TreeList.<RegistryByteBuf, Iota>streamCodecOp().apply(IotaType.TYPED_STREAM_CODEC), FrameGetItems::getBaseStack,
            PacketCodecs.collection(ArrayList::new, ITEM_SLOT_PACKET), FrameGetItems::getToCheck,
            PacketCodecs.optional(ITEM_SLOT_PACKET), a -> {
                var ret = a.getOldReturn();
                        if (ret == null) {
                            return Optional.empty();
                        } else {
                            return Optional.of(ret);
                        }
                    },
            PacketCodecs.STRING, a -> a.isFirst().name(),
            (code, baseStack, toCheck, oldReturn, jankyMaybe) ->
                new FrameGetItems(code, baseStack, toCheck, OptionalsKt.getOrNull(oldReturn), JankyMaybe.valueOf(JankyMaybe.class, jankyMaybe))
    );

    public static final MapCodec<FrameCheckItems> FRAME_CHECK_ITEMS_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("code").forGetter(FrameCheckItems::getCode),
            TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("baseStack").forGetter(FrameCheckItems::getBaseStack),
            ITEM_SLOT_CODEC.listOf().fieldOf("toCheck").forGetter(FrameCheckItems::getToCheck),
            Codecs.ESCAPED_STRING.fieldOf("jankyMaybe").forGetter(a -> a.isFirst().toString())
    ).apply(instance, (code, baseStack, toCheck, jankyMaybe) ->
            new FrameCheckItems(code, baseStack, toCheck, JankyMaybe.valueOf(JankyMaybe.class, jankyMaybe))));

    public static final PacketCodec<RegistryByteBuf, FrameCheckItems> FRAME_CHECK_ITEMS_PACKET_CODEC = PacketCodec.tuple(
            TreeList.<RegistryByteBuf, Iota>streamCodecOp().apply(IotaType.TYPED_STREAM_CODEC), FrameCheckItems::getCode,
            TreeList.<RegistryByteBuf, Iota>streamCodecOp().apply(IotaType.TYPED_STREAM_CODEC), FrameCheckItems::getBaseStack,
            PacketCodecs.collection(ArrayList::new, ITEM_SLOT_PACKET), FrameCheckItems::getToCheck,
            PacketCodecs.STRING, a -> a.isFirst().name(),
            (code, baseStack, toCheck, jankyMaybe) ->
                    new FrameCheckItems(code, baseStack, toCheck, JankyMaybe.valueOf(JankyMaybe.class, jankyMaybe))
    );

    public static final MapCodec<FrameBreakBlockLoci> FRAME_BREAK_BLOCK_LOCI_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("code").forGetter(FrameBreakBlockLoci::getCode),
            BlockPos.CODEC.fieldOf("pos").forGetter(FrameBreakBlockLoci::getBlockBreakingLocus),
            TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("baseStack").forGetter(FrameBreakBlockLoci::getBaseStack),
            BlockPos.CODEC.listOf().fieldOf("toCheck").forGetter(FrameBreakBlockLoci::getToCheck),
            BlockPos.CODEC.optionalFieldOf("oldReturn").forGetter(a -> {
                var ret = a.getOldReturn();
                if (ret == null) {
                    return Optional.empty();
                } else {
                    return Optional.of(ret);
                }
            }),
            Codecs.ESCAPED_STRING.fieldOf("jankyMaybe").forGetter(a -> a.isFirst().name()),
            ItemStack.CODEC.fieldOf("itemStack").forGetter(FrameBreakBlockLoci::getItemStack)
    ).apply(instance, (code, locus, baseStack, toCheck, oldReturn, jankyMaybe, stack) ->
            new FrameBreakBlockLoci(code, locus, baseStack, toCheck, OptionalsKt.getOrNull(oldReturn), JankyMaybe.valueOf(JankyMaybe.class, jankyMaybe), stack)));

    public static final PacketCodec<RegistryByteBuf, FrameBreakBlockLoci> FRAME_BREAK_BLOCK_LOCI_PACKET_CODEC = PacketCodec.tuple(
            TreeList.<RegistryByteBuf, Iota>streamCodecOp().apply(IotaType.TYPED_STREAM_CODEC), FrameBreakBlockLoci::getCode,
            BlockPos.PACKET_CODEC, FrameBreakBlockLoci::getBlockBreakingLocus,
            TreeList.<RegistryByteBuf, Iota>streamCodecOp().apply(IotaType.TYPED_STREAM_CODEC), FrameBreakBlockLoci::getBaseStack,
            PacketCodecs.collection(ArrayList::new, BlockPos.PACKET_CODEC), FrameBreakBlockLoci::getToCheck,
            PacketCodecs.optional(BlockPos.PACKET_CODEC), a -> {
                var ret = a.getOldReturn();
                if (ret == null) {
                    return Optional.empty();
                } else {
                    return Optional.of(ret);
                }
            },
            WoopsTooMany.WOOPS_TOO_MANY_PACKET_CODEC, a -> new WoopsTooMany(a.isFirst(), a.getItemStack()),
            (code, pos, baseStack, toCheck, oldReturn, many) ->
                    new FrameBreakBlockLoci(code, pos, baseStack, toCheck, OptionalsKt.getOrNull(oldReturn), many.maybe, many.stack)
    );

    private record WoopsTooMany(JankyMaybe maybe, ItemStack stack){
        private static final PacketCodec<RegistryByteBuf, WoopsTooMany> WOOPS_TOO_MANY_PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, WoopsTooMany::maybeStr,
                ItemStack.PACKET_CODEC, WoopsTooMany::stack,
                (a, b) ->
                        new WoopsTooMany(JankyMaybe.valueOf(JankyMaybe.class, a), b)
        );

        private String maybeStr(){
            return this.maybe.toString();
        }
    }

    public static ItemStackIota makeIota(ItemStack stack, RegistryWrapper.WrapperLookup lookup){
        // I hope you can see why this is in the Codecs class. Sadly.
        //var codec = MoreIotasIotaTypes.ITEM_STACK.codec().codec();
        //NbtCompound compound = new NbtCompound();
        //NbtCompound item = new NbtCompound();
        //stack.encode(lookup, item);
        //compound.put("itemstack", item);
        return ItemStackIota.createFiltered(stack); //codec.decode(NbtOps.INSTANCE, compound).getOrThrow().getFirst();
    }
}
