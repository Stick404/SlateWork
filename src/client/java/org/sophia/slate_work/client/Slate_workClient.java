package org.sophia.slate_work.client;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import org.sophia.slate_work.Slate_work;
import org.sophia.slate_work.client.blockEntityRenders.HotbarLocirenderer;
import org.sophia.slate_work.client.blockEntityRenders.MacroLociRenderer;
import org.sophia.slate_work.client.blockEntityRenders.SaveLociRenderer;
import org.sophia.slate_work.client.lens.*;
import org.sophia.slate_work.client.screen.Ghost3x3Screen;
import org.sophia.slate_work.client.screen.HotbarLociScreen;
import org.sophia.slate_work.registries.BlockRegistry;

public class Slate_workClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(Slate_work.GHOST_3X3_SCREEN, Ghost3x3Screen::new);
        HandledScreens.register(Slate_work.HOTBAR_LOCI_SCREEN, HotbarLociScreen::new);

        BlockEntityRendererFactories.register(BlockRegistry.MACRO_LOCI_ENTITY, MacroLociRenderer::new);
        BlockEntityRendererFactories.register(BlockRegistry.SAVE_LOCI_ENTITY, SaveLociRenderer::new);
        BlockEntityRendererFactories.register(BlockRegistry.HOTBAR_LOCI_ENTITY, HotbarLocirenderer::new);

        ScryingLensOverlayRegistry.addDisplayer(BlockRegistry.MACRO_LOCI, new MacroLociScrying());
        ScryingLensOverlayRegistry.addDisplayer(BlockRegistry.STORAGE_LOCI, new StorageLociScrying());
        ScryingLensOverlayRegistry.addDisplayer(BlockRegistry.SENTINEL_LOCI, new SentinelLociScrying());
        ScryingLensOverlayRegistry.addDisplayer(BlockRegistry.BROADCASTER_LOCI, new BroadcasterLociScrying());
        ScryingLensOverlayRegistry.addDisplayer(BlockRegistry.SAVE_LOCI, new SaveLociScryingKT());
    }
}
