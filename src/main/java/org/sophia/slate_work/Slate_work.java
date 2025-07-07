package org.sophia.slate_work;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import net.fabricmc.api.ModInitializer;
import org.sophia.slate_work.casting.AmbitPushing;
import org.sophia.slate_work.registries.BlockRegistry;
import org.sophia.slate_work.registries.PatternRegistry;

public class Slate_work implements ModInitializer {
    public static final String MOD_ID = "slate_work";

    @Override
    public void onInitialize() {
        BlockRegistry.init();
        PatternRegistry.init();

        CastingEnvironment.addCreateEventListener( (a,b) -> a.addExtension(new AmbitPushing(a)));
    }
}
