package org.sophia.slate_work.registries;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.hex.HexActions;
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.sophia.slate_work.casting.actions.OpGetStorageLoci;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.sophia.slate_work.Slate_work.MOD_ID;

public class PatternRegistry {
    private static final Map<Identifier, ActionRegistryEntry> PATTERNS = new LinkedHashMap<>();

    public static void init(){
        for (Map.Entry<Identifier, ActionRegistryEntry> entry : PATTERNS.entrySet()) {
            Registry.register(HexActions.REGISTRY, entry.getKey(), entry.getValue());
        }
    }

    public static final HexPattern GET_STORAGE = make("eaqwqaeqqdeewweedq",HexDir.SOUTH_WEST,"get_storage", OpGetStorageLoci.INSTANCE);

    private static HexPattern make(String sig, HexDir dir, String name, Action spell){
        PATTERNS.put(new Identifier(MOD_ID,name), new ActionRegistryEntry(HexPattern.fromAngles(sig,dir),spell));
        return HexPattern.fromAngles(sig,dir);
    }
}
