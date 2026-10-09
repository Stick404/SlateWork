package org.sophia.slate_work.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.sophia.slate_work.Slate_work.MOD_ID;

public class SlateWorksComponents {
    private static final Map<Identifier, ComponentType<?>> COMPONENTS = new LinkedHashMap<>();

    public static void init(){
        for (Map.Entry<Identifier, ComponentType<?>> entry : COMPONENTS.entrySet()) {
            Registry.register(Registries.DATA_COMPONENT_TYPE, entry.getKey(), entry.getValue());
        }
    }

    public static final ComponentType<WhisperingStoneComponent> WHISPERING_STONE_COMPONENT = make("whispering_stone_component", ComponentType.<WhisperingStoneComponent>builder()
            .codec(WhisperingStoneComponent.CODEC)
            .packetCodec(WhisperingStoneComponent.PACKET_CODEC)
            .build());

    private static <T> ComponentType<T> make(String id, ComponentType<T> component) {
        var old = COMPONENTS.put(Identifier.of(MOD_ID, id), component);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return component;
    }


    public record WhisperingStoneComponent(String string, BlockPos pos, Identifier dim){
        public static Codec<WhisperingStoneComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PrimitiveCodec.STRING.fieldOf("string").forGetter(WhisperingStoneComponent::string),
                BlockPos.CODEC.fieldOf("pos").forGetter(WhisperingStoneComponent::pos),
                Identifier.CODEC.fieldOf("dim").forGetter(WhisperingStoneComponent::dim)
        ).apply(instance, WhisperingStoneComponent::new));

        public static PacketCodec<ByteBuf, WhisperingStoneComponent> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, WhisperingStoneComponent::string,
                BlockPos.PACKET_CODEC, WhisperingStoneComponent::pos,
                Identifier.PACKET_CODEC, WhisperingStoneComponent::dim,
                WhisperingStoneComponent::new
        );
    }
}
