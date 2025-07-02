package org.sophia.slate_work;

import net.fabricmc.api.ModInitializer;
import org.sophia.slate_work.registries.BlockRegistry;
import org.sophia.slate_work.registries.PatternRegistry;

public class Slate_work implements ModInitializer {
    public static final String MOD_ID = "slate_work";

    @Override
    public void onInitialize() {
        BlockRegistry.init();
        PatternRegistry.init();
    }
}
