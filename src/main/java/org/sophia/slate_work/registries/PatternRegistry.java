package org.sophia.slate_work.registries;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.hex.HexActions;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.binary.Hex;
import org.sophia.slate_work.casting.actions.OpContainsItem;
import org.sophia.slate_work.casting.actions.OpGetItem;
import org.sophia.slate_work.casting.actions.OpGetStorageLoci;
import org.sophia.slate_work.casting.actions.OpStoreItem;

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
    public static final HexPattern STORE_ITEM = make("eaqwqaeqwaeaeqqeaeaw",HexDir.SOUTH_WEST,"store_item", OpStoreItem.INSTANCE);
    public static final HexPattern GET_ITEM = make("eaqwqaeqwqqwqwwqwqqweqwaweadwawwwawdaewawq",HexDir.SOUTH_WEST,"get_item", OpGetItem.INSTANCE);
    public static final HexPattern CHECK_ITEM = make("eaqwqaeqeedqwa",HexDir.SOUTH_WEST,"check_item", OpContainsItem.INSTANCE);

    private static HexPattern make(String sig, HexDir dir, String name, Action spell){
        PATTERNS.put(new Identifier(MOD_ID,name), new ActionRegistryEntry(HexPattern.fromAngles(sig,dir),spell));
        return HexPattern.fromAngles(sig,dir);
    }
}
