package org.sophia.slate_work.registries;

import at.petrak.hexcasting.api.casting.eval.vm.ImageComponentType;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.math.HexSignature;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import org.sophia.slate_work.Slate_work;

import java.util.*;

public class SlateWorksImageComponents {
    private static final Map<Identifier, ImageComponentType<?>> COMPONENTS = new LinkedHashMap<>();
    public static final Registry<ImageComponentType<?>> REGISTRY = IXplatAbstractions.INSTANCE.getImageComponentRegistry();

    private SlateWorksImageComponents(){}

    public static final ImageComponentType<UUID> LISTENING_IMPETUS = register("listening_impetus",
            new ImageComponentType<>(Uuids.CODEC, Uuids.PACKET_CODEC, false));

    public static final ImageComponentType<Integer> ACCELERATOR_LOCI = register("accelerator_loci",
            new ImageComponentType<>(Codec.INT, PacketCodecs.INTEGER, true));

    public static final ImageComponentType<AmbitLociComponent> AMBIT_LOCI = register("ambit_loci",
            new ImageComponentType<>(AmbitLociComponent.CODEC, AmbitLociComponent.PACKET_CODEC, false));

    public static final ImageComponentType<BlockPos> HOTBAR_LOCI = register("hotbar_loci",
            new ImageComponentType<>(BlockPos.CODEC, BlockPos.PACKET_CODEC, false));

    public static final ImageComponentType<MacroLociComponent> MACRO_LOCI = register("macro_loci",
            new ImageComponentType<>(MacroLociComponent.CODEC, MacroLociComponent.PACKET_CODEC, false));

    public static final ImageComponentType<List<BlockPos>> SENTINEL_LOCI = register("sentinel_loci",
            new ImageComponentType<>(BlockPos.CODEC.listOf(), PacketCodecs.collection(ArrayList::new, BlockPos.PACKET_CODEC), false));

    // Since this is marked as "transient," we don't need to manage the world-time checks
    public static final ImageComponentType<Long> SENTINEL_COUNT = register("sentinel_count",
            new ImageComponentType<>(Codec.LONG, PacketCodecs.VAR_LONG, true));

    public static final ImageComponentType<Integer> SPEED_LOCI = register("speed_loci",
            new ImageComponentType<>(Codec.INT, PacketCodecs.INTEGER, false));

    public static final ImageComponentType<List<BlockPos>> STORAGE_LOCI = register("storage_loci",
            new ImageComponentType<>(BlockPos.CODEC.listOf(), PacketCodecs.collection(ArrayList::new, BlockPos.PACKET_CODEC), false));

    public static final ImageComponentType<Double> MUTE_LOCI = register("mute_loci",
            new ImageComponentType<>(Codec.DOUBLE, PacketCodecs.DOUBLE, false));

    public static void init(){
        for (var e : COMPONENTS.entrySet()){
            Registry.register(REGISTRY,e.getKey(),e.getValue());
        }
    }

    // Who would be copying Hex Code :clueless:
    private static <T> ImageComponentType<T> register(String name, ImageComponentType<T> continuation) {
        var old = COMPONENTS.put(Identifier.of(Slate_work.MOD_ID, name), continuation);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return continuation;
    }

    public record MacroLociComponent(Map<String, Iota> map){
        public static final Codec<MacroLociComponent> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Codec.unboundedMap(Codec.STRING, IotaType.TYPED_CODEC).fieldOf("map").forGetter(MacroLociComponent::map)
                        ).apply(instance, a -> new MacroLociComponent(new HashMap<>(a)))
                );
        public static final PacketCodec<RegistryByteBuf, MacroLociComponent> PACKET_CODEC =
                PacketCodec.tuple(
                        PacketCodecs.map(HashMap::new, PacketCodecs.STRING, IotaType.TYPED_STREAM_CODEC), MacroLociComponent::map,
                        MacroLociComponent::new
                );
    }

    public record AmbitLociComponent(BlockPos positive, BlockPos negative){
        public static final Codec<AmbitLociComponent> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                                BlockPos.CODEC.fieldOf("positive").forGetter(AmbitLociComponent::positive),
                                BlockPos.CODEC.fieldOf("negative").forGetter(AmbitLociComponent::negative)
                        ).apply(instance, AmbitLociComponent::new)
                );
        public static final PacketCodec<RegistryByteBuf, AmbitLociComponent> PACKET_CODEC =
                PacketCodec.tuple(
                        BlockPos.PACKET_CODEC, AmbitLociComponent::positive,
                        BlockPos.PACKET_CODEC, AmbitLociComponent::negative,
                        AmbitLociComponent::new
                );
    }
}
