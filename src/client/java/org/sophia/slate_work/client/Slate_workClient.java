package org.sophia.slate_work.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.sophia.slate_work.Slate_work;
import org.sophia.slate_work.client.screen.Ghost3x3Screen;
import org.sophia.slate_work.registries.BlockRegistry;

public class Slate_workClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(Slate_work.GHOST_3X3_SCREEN, Ghost3x3Screen::new);
        BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.STORAGE_LOCI, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.CRAFTING_LOCI, RenderLayer.getTranslucent());
    }
}
