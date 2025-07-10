package org.sophia.slate_work.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.sophia.slate_work.GUI.Ghost3x3ScreenHandler;

import static org.sophia.slate_work.Slate_work.MOD_ID;

public class Ghost3x3Screen extends HandledScreen<Ghost3x3ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(MOD_ID,"textures/gui/crafting_loci.png");

    public Ghost3x3Screen(Ghost3x3ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleY = 100000;
        this.playerInventoryTitleX = (this.backgroundWidth - this.textRenderer.getWidth(this.playerInventoryTitle)) / 2;
        this.playerInventoryTitleY = 74;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }
}
