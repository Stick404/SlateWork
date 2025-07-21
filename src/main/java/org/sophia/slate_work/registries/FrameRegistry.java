package org.sophia.slate_work.registries;

import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.sophia.slate_work.casting.contuinations.FrameGetItems;

import java.util.LinkedHashMap;
import java.util.Map;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class FrameRegistry {
    private static final Map<Identifier, ContinuationFrame.Type<?>> CONTINUATIONS = new LinkedHashMap<>();
    public static final Registry<ContinuationFrame.Type<?>> REGISTRY = IXplatAbstractions.INSTANCE.getContinuationTypeRegistry();

    public static void init(){
        for (var e : CONTINUATIONS.entrySet()){
            Registry.register(REGISTRY,e.getKey(),e.getValue());
        }
    }

    public static final ContinuationFrame.Type<@NotNull FrameGetItems> GET_ITEM = continuation("search", FrameGetItems.TYPE);


    // Who would be copying Hex Code :clueless:
    private static <U extends ContinuationFrame, T extends ContinuationFrame.Type<U>> T continuation(String name, T continuation) {
        var old = CONTINUATIONS.put(modLoc(name), continuation);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return continuation;
    }
}
