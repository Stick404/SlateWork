package org.sophia.slate_work.client;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import org.sophia.slate_work.Slate_work;
import org.sophia.slate_work.client.blockEntityRenders.MacroLociRenderer;
import org.sophia.slate_work.client.lens.MacroLociScrying;
import org.sophia.slate_work.client.lens.StorageLociScrying;
import org.sophia.slate_work.client.screen.Ghost3x3Screen;
import org.sophia.slate_work.registries.BlockRegistry;

public class Slate_workClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(Slate_work.GHOST_3X3_SCREEN, Ghost3x3Screen::new);
        BlockEntityRendererFactories.register(BlockRegistry.MACRO_LOCI_ENTITY, MacroLociRenderer::new);
        ScryingLensOverlayRegistry.addDisplayer(BlockRegistry.MACRO_LOCI, new MacroLociScrying());
        ScryingLensOverlayRegistry.addDisplayer(BlockRegistry.STORAGE_LOCI, new StorageLociScrying());
    }
}
