package org.sophia.slate_work;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import gay.object.ioticblocks.IoticBlocks;
import gay.object.ioticblocks.api.IoticBlocksAPI;
import miyucomics.hexpose.iotas.TextIota;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import org.sophia.slate_work.GUI.Ghost3x3ScreenHandler;
import org.sophia.slate_work.blocks.impetus.ListeningImpetusEntity;
import org.sophia.slate_work.casting.CircleAmbitChanges;
import org.sophia.slate_work.misc.KnownBroadcasters;
import org.sophia.slate_work.registries.BlockRegistry;
import org.sophia.slate_work.registries.FrameRegistry;
import org.sophia.slate_work.registries.PatternRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public class Slate_work implements ModInitializer {
    public static final String MOD_ID = "slate_work";
    public static final Logger LOGGER = Logger.getLogger("Slate Works");
    public static final HashMap<BlockPos, ListeningImpetusEntity> LISTENERS = new HashMap<>();

    public static ScreenHandlerType<Ghost3x3ScreenHandler> GHOST_3X3_SCREEN = Registry.register(Registries.SCREEN_HANDLER,
            new Identifier(MOD_ID,"ghost3x3screen"),
            new ExtendedScreenHandlerType<>(Ghost3x3ScreenHandler::new));

    @Override
    public void onInitialize() {
        BlockRegistry.init();
        PatternRegistry.init();
        FrameRegistry.init();

        CastingEnvironment.addCreateEventListener( (a,b) -> a.addExtension(new CircleAmbitChanges(a)));

        ServerLifecycleEvents.SERVER_STOPPING.register(new ClearBroadcasters());
        ServerLifecycleEvents.SERVER_STARTED.register((a) -> { // DO NOT LOAD ON THE CLIENT
            ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
                var compared = message.getContent().getString();
                for (var listener : LISTENERS.entrySet()) {
                    var entity = listener.getValue();
                    var pos = listener.getKey();
                    if (entity.isRunning()){
                        continue;
                    }
                    if (sender.squaredDistanceTo(pos.toCenterPos()) < 16*16 & compared.startsWith(entity.getString())) { //Doing the loaded check *just* in case
                        String string = compared.substring(entity.getString().length()).stripLeading();
                        if (string.isBlank()) string = " ";

                        entity.setIotas(new EntityIota(sender), new TextIota(Text.literal(string)));
                        entity.startExecution(sender);
                        entity.sync();
                        return false;
                    }
                }
                return true;
            });
        });

        if (FabricLoader.getInstance().isModLoaded("ioticblocks")) {
            SlateWorksIoticBlocks.init();
        }
    }

    private static class ClearBroadcasters implements ServerLifecycleEvents.ServerStopping {
        @Override
        public void onServerStopping(MinecraftServer server) {
            KnownBroadcasters.INSTANCE.clear();
        }
    }
}