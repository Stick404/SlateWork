package org.sophia.slate_work.client;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.annotation.ClientFieldsAreNonnullByDefault;
import org.sophia.slate_work.Slate_work;
import org.sophia.slate_work.client.blockEntityRenders.HotbarLociRenderer;
import org.sophia.slate_work.client.blockEntityRenders.MacroLociRenderer;
import org.sophia.slate_work.client.blockEntityRenders.SaveLociRenderer;
import org.sophia.slate_work.client.lens.*;
import org.sophia.slate_work.client.screen.Ghost3x3Screen;
import org.sophia.slate_work.client.screen.HotbarLociScreen;
import org.sophia.slate_work.registries.SlateWorksBlockRegistry;

@ClientFieldsAreNonnullByDefault
public class Slate_workClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(SlateWorksBlockRegistry.BLOCK_BREAKING_LOCI, RenderLayer.getTranslucent());

        HandledScreens.register(Slate_work.GHOST_3X3_SCREEN, Ghost3x3Screen::new);
        HandledScreens.register(Slate_work.HOTBAR_LOCI_SCREEN, HotbarLociScreen::new);

        BlockEntityRendererRegistry.register(SlateWorksBlockRegistry.MACRO_LOCI_ENTITY, MacroLociRenderer::new);
        BlockEntityRendererRegistry.register(SlateWorksBlockRegistry.SAVE_LOCI_ENTITY, SaveLociRenderer::new);
        BlockEntityRendererRegistry.register(SlateWorksBlockRegistry.HOTBAR_LOCI_ENTITY, HotbarLociRenderer::new);

        ScryingLensOverlayRegistry.addDisplayer(SlateWorksBlockRegistry.MACRO_LOCI, new MacroLociScrying());
        ScryingLensOverlayRegistry.addDisplayer(SlateWorksBlockRegistry.STORAGE_LOCI, new StorageLociScrying());
        ScryingLensOverlayRegistry.addDisplayer(SlateWorksBlockRegistry.SENTINEL_LOCI, new SentinelLociScrying());
        ScryingLensOverlayRegistry.addDisplayer(SlateWorksBlockRegistry.BROADCASTER_LOCI, new BroadcasterLociScrying());
        ScryingLensOverlayRegistry.addDisplayer(SlateWorksBlockRegistry.SAVE_LOCI, new SaveLociScryingKT());
        ScryingLensOverlayRegistry.addDisplayer(SlateWorksBlockRegistry.FAKE_PLAYER_LOCI, new FakePlayerLociScrying());
        ScryingLensOverlayRegistry.addDisplayer(SlateWorksBlockRegistry.TRADE_LOCI, new TradeLociScrying());
        ScryingLensOverlayRegistry.addDisplayer(SlateWorksBlockRegistry.BLOCK_BREAKING_LOCI, new BlockBreakLociScrying());
    }
}
