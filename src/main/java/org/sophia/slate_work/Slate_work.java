package org.sophia.slate_work;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.common.lib.HexSounds;
import miyucomics.hexpose.iotas.TextIota;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.core.util.CyclicBuffer;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.GUI.Ghost3x3ScreenHandler;
import org.sophia.slate_work.blocks.impetus.ListeningImpetusEntity;
import org.sophia.slate_work.casting.CircleAmbitChanges;
import org.sophia.slate_work.item.WhisperingStone;
import org.sophia.slate_work.misc.KnownBroadcasters;
import org.sophia.slate_work.registries.BlockRegistry;
import org.sophia.slate_work.registries.FrameRegistry;
import org.sophia.slate_work.registries.PatternRegistry;

import java.util.HashMap;
import java.util.Optional;
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
            ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) ->{
                    var z = willBlock(message.getSignedContent(), sender);
                    if (z.entity.isPresent()){
                        var entity = z.entity.get();
                        entity.setIotas(new EntityIota(sender), new TextIota(Text.literal(z.string)));
                        entity.startExecution(sender);
                        entity.sync();
                    }
                    if (z.blocked && !z.failed) {
                        if (z.item && z.entity.isEmpty()) sender.playSound(HexSounds.FLIGHT_FINISH, SoundCategory.PLAYERS, 1f, 1.5f);
                        else sender.playSound(HexSounds.READ_LORE_FRAGMENT, SoundCategory.PLAYERS, 1f, 2f);
                    } else if (z.failed && z.whispering.isPresent()) {
                        var stack = z.whispering.get();
                        sender.playSound(HexSounds.CAST_FAILURE, SoundCategory.PLAYERS, 1f, 1f);
                        stack.removeSubNbt("cords");
                        stack.removeSubNbt("string");
                    }
                    return !z.blocked;
            });


        if (FabricLoader.getInstance().isModLoaded("ioticblocks")) {
            SlateWorksIoticBlocks.init();
        }
    }

    public ShouldRun willBlock(String compared, ServerPlayerEntity sender){
        boolean found = false; // if it found a Librarian for the Whispering Stone
        boolean running = false;
        var ref = new Object() { // Hate. But. It works
            ItemStack stack;
        };

        sender.getHandItems().iterator().forEachRemaining((item) -> {
            if (item.isOf(BlockRegistry.WHISPERING_STONE)) {
                ref.stack = item;
            }
        });

        if (ref.stack != null) {
            var cordNBT = ref.stack.getSubNbt("cords");
            if (cordNBT != null && sender.getWorld().getBlockEntity(NbtHelper.toBlockPos(cordNBT)) instanceof ListeningImpetusEntity entity) {
                found = true;
                if (compared.startsWith(entity.getString() + " ") ^ compared.equals(entity.getString())){
                    if (entity.isRunning()) {
                        return new ShouldRun(true, true, "", Optional.empty(), false, Optional.of(ref.stack));
                    } else {
                        String string = compared.substring(entity.getString().length()).stripLeading();
                        if (string.isBlank()) string = " ";
                        return new ShouldRun(true, true, string, Optional.of(entity), false, Optional.of(ref.stack));
                    }
                }
            }
        }

        for (var listener : LISTENERS.entrySet()) {
            var entity = listener.getValue();
            var pos = listener.getKey();
            if (entity.isRunning()){
                continue;
            }
            if (sender.squaredDistanceTo(pos.toCenterPos()) < 16*16 && (compared.startsWith(entity.getString() + " ") ^ compared.equals(entity.getString()))) {
                String string = compared.substring(entity.getString().length()).stripLeading();
                if (string.isBlank()) string = " ";
                return new ShouldRun(true, false, string, Optional.of(entity), false, Optional.empty());
            }
        }

        if (ref.stack != null && ref.stack.getSubNbt("cords") != null && !found){ // Somehow it fails and is binded
            return new ShouldRun(true, true, "", Optional.empty(), true, Optional.of(ref.stack));
        }
        return new ShouldRun(running, false, "", Optional.empty(), false, Optional.empty());
    }

    private static class ClearBroadcasters implements ServerLifecycleEvents.ServerStopping {
        @Override
        public void onServerStopping(MinecraftServer server) {
            KnownBroadcasters.INSTANCE.clear();
        }
    }
    /**
     * @param blocked is if the message should be blocked
     * @param item is if it was casted via the item
     * @param string is the message that will be sent to the Listening Impetus Entity
     * @param entity is the Listening Impetus Entity
     * @param failed is if the item "hard failed" (IE: if it should be cleared)
     * @param whispering is the Whispering Stone that was used
     *
     * **/
    public record ShouldRun(boolean blocked, boolean item, String string, Optional<ListeningImpetusEntity> entity, boolean failed, Optional<ItemStack> whispering){}
}