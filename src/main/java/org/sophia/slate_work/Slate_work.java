package org.sophia.slate_work;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.sophia.slate_work.GUI.Ghost3x3ScreenHandler;
import org.sophia.slate_work.casting.CircleAmbitChanges;
import org.sophia.slate_work.registries.BlockRegistry;
import org.sophia.slate_work.registries.FrameRegistry;
import org.sophia.slate_work.registries.PatternRegistry;

import java.util.logging.Logger;

public class Slate_work implements ModInitializer {
    public static final String MOD_ID = "slate_work";
    public static final Logger LOGGER = Logger.getLogger("Slate Works");

    public static ScreenHandlerType<Ghost3x3ScreenHandler> GHOST_3X3_SCREEN = Registry.register(Registries.SCREEN_HANDLER,
            new Identifier(MOD_ID,"ghost3x3screen"),
            new ExtendedScreenHandlerType<>(Ghost3x3ScreenHandler::new));

    @Override
    public void onInitialize() {
        BlockRegistry.init();
        PatternRegistry.init();
        FrameRegistry.init();

        CastingEnvironment.addCreateEventListener( (a,b) -> a.addExtension(new CircleAmbitChanges(a)));
    }
}
