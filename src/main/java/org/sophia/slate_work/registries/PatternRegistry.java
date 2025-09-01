package org.sophia.slate_work.registries;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.hex.HexActions;
import net.minecraft.block.BlockState;
import net.minecraft.block.PumpkinBlock;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.sophia.slate_work.casting.actions.*;
import org.sophia.slate_work.casting.actions.hotbar.OpGetItems;
import org.sophia.slate_work.casting.actions.hotbar.OpSetSlot;
import org.sophia.slate_work.casting.actions.sentinel.OpGetSents;
import org.sophia.slate_work.casting.actions.sentinel.OpSetSents;
import org.sophia.slate_work.casting.actions.storage.*;
import org.sophia.slate_work.mixins.MixinCircleExecInvoker;

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


    public static final HexPattern STORE_ITEM = make("eaqwqaeqwaeaeqqeaeaw", HexDir.SOUTH_WEST, "store_item", OpStoreItem.INSTANCE);
    public static final HexPattern GET_STORAGE = make("eaqwqaeqqdeewweedq", HexDir.SOUTH_WEST, "get_storage", OpGetStorageLoci.INSTANCE);
    public static final HexPattern GET_ITEM = make("eaqwqaeqwqqwqwwqwqqweqwaweadwawwwawdaewawq", HexDir.SOUTH_WEST, "get_item", OpGetItem.INSTANCE);
    // Woah, a comment
    public static final HexPattern CHECK_ITEM = make("eaqwqaeqqddqeeqddq", HexDir.SOUTH_WEST, "check_item", OpCheckItem.INSTANCE);
    public static final HexPattern SORT_ITEMS = make("eaqwqaeqqwaeadaeawq", HexDir.SOUTH_WEST, "sort_items", OpSortStorageLoci.INSTANCE);

    public static final HexPattern SET_CRAFT = make("eaqwqaeqwaeadawwadaeaw", HexDir.SOUTH_WEST, "set_craft", OpSetCraftingLoci.INSTANCE);
    public static final HexPattern SET_MARCO = make("qqqwqqqqqaqeeaqwqae", HexDir.WEST, "set_macro", OpSetMacro.INSTANCE);

    public static final HexPattern SET_SENTS = make("waeawaewawwa", HexDir.EAST, "set_sents", OpSetSents.INSTANCE);
    public static final HexPattern GET_SENTS = make("waeawaewawwaeq", HexDir.EAST, "get_sents", OpGetSents.INSTANCE);

    public static final HexPattern READ_BROADCAST = make("aqwqaweeeeewwaaw", HexDir.WEST, "read_broadcast", OpReadBroadcast.INSTANCE);

    public static final HexPattern SET_SLOT = make("eaqwqaeqawawa", HexDir.SOUTH_WEST, "set_slot", OpSetSlot.INSTANCE);
    public static final HexPattern GET_ITEMS = make("eaqwqaeqawawaedd", HexDir.SOUTH_WEST, "get_items", OpGetItems.INSTANCE);

    // Got permission from Walks to add these to Slate Works
    public static final HexPattern WAVE_POSITION = make("eaqdaadqaeeaa", HexDir.SOUTH_WEST, "wave_position",
            new CircleReflection((env) -> new Vec3Iota(env.circleState().currentPos.toCenterPos())));
    public static final HexPattern WAVE_NORMAL = make("eaqdaadqaeewa", HexDir.SOUTH_WEST, "wave_normal",
            new CircleReflection(((env ->{
                BlockState block = env.getWorld().getBlockState(env.circleState().currentPos);
                if (block.getBlock() instanceof BlockCircleComponent slate) {
                    var pos = slate.normalDir(env.circleState().currentPos,block,env.getWorld()).getVector();
                    return new Vec3Iota(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
                }
                return new Vec3Iota(new Vec3d(0,0,0));
            }))));
    public static final HexPattern WAVE_SPEED = make("eaqdaadqaeewq", HexDir.SOUTH_WEST, "wave_speed",
            new CircleReflection( env -> new DoubleIota(((MixinCircleExecInvoker) env.circleState()).slate_work$getTickSpeed())));
    public static final HexPattern MEDIA_REFLECTION = make("eaqdaadqae", HexDir.SOUTH_WEST, "media_reflection",
            new CircleReflection(env -> new DoubleIota(env.getImpetus().getMedia()/10000f)));

    private static HexPattern make(String sig, HexDir dir, String name, Action spell){
        PATTERNS.put(new Identifier(MOD_ID,name), new ActionRegistryEntry(HexPattern.fromAngles(sig,dir),spell));
        return HexPattern.fromAngles(sig,dir);
    }
}
