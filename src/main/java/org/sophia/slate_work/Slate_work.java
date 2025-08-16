package org.sophia.slate_work;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.common.lib.HexSounds;
import com.mojang.serialization.Codec;
import miyucomics.hexpose.iotas.TextIota;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.sophia.slate_work.GUI.Ghost3x3ScreenHandler;
import org.sophia.slate_work.casting.CircleAmbitChanges;
import org.sophia.slate_work.misc.ChatHelper;
import org.sophia.slate_work.misc.KnownBroadcasters;
import org.sophia.slate_work.registries.BlockRegistry;
import org.sophia.slate_work.registries.FrameRegistry;
import org.sophia.slate_work.registries.PatternRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("UnstableApiUsage")
public class Slate_work implements ModInitializer {
    public static final String MOD_ID = "slate_work";
    public static final Logger LOGGER = Logger.getLogger("Slate Works");
    public static final AttachmentType<List<BlockPos>> chunk_listeners = AttachmentRegistry.<List<BlockPos>>builder()
            .initializer(ArrayList::new)
            .copyOnDeath().persistent(Codec.list(BlockPos.CODEC))
            .buildAndRegister(new Identifier(MOD_ID, "listening_attachment"));

    public static ScreenHandlerType<Ghost3x3ScreenHandler> GHOST_3X3_SCREEN = Registry.register(Registries.SCREEN_HANDLER,
            new Identifier(MOD_ID,"ghost3x3screen"),
            new ExtendedScreenHandlerType<>(Ghost3x3ScreenHandler::new));

    public static ChatHelper.ShouldRun getCheck(){
        return ChatHelper.getHelper().LAST_CHECK;
    }

    @Override
    public void onInitialize() {
        BlockRegistry.init();
        PatternRegistry.init();
        FrameRegistry.init();

        CastingEnvironment.addCreateEventListener( (a,b) -> a.addExtension(new CircleAmbitChanges(a)));

        ServerLifecycleEvents.SERVER_STOPPING.register(new ClearBroadcasters());
            ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) ->{
                    var z = ChatHelper.getHelper().willBlock(message.getSignedContent(), sender);
                    if (z.entity().isPresent()){
                        var entity = z.entity().get();
                        String string = z.string().substring(entity.getString().length()).stripLeading();
                        if (string.isBlank()) string = " ";
                        entity.setIotas(new EntityIota(sender), new TextIota(Text.literal(string)));
                        entity.startExecution(sender);
                        entity.sync();
                    }
                    if (!z.failed() && z.blocked()) {
                        if (z.item() && z.entity().isEmpty()) sender.playSound(HexSounds.FLIGHT_FINISH, SoundCategory.PLAYERS, 1f, 1.5f);
                        else sender.playSound(HexSounds.READ_LORE_FRAGMENT, SoundCategory.PLAYERS, 1f, 2f);
                    } else if (z.failed() && z.whispering().isPresent() && z.entity().isEmpty() && z.blocked()) {
                        var stack = z.whispering().get();
                        sender.playSound(HexSounds.CAST_FAILURE, SoundCategory.PLAYERS, 1f, 1f);
                        stack.removeSubNbt("cords");
                        stack.removeSubNbt("string");
                    }
                    return !z.blocked();
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