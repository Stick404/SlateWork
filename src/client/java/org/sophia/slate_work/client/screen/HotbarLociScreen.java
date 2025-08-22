package org.sophia.slate_work.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec2f;
import org.sophia.slate_work.GUI.HotbarLociScreenHandler;
import org.sophia.slate_work.blocks.entities.HotbarLociEntity;

import static org.sophia.slate_work.Slate_work.MOD_ID;

public class HotbarLociScreen extends HandledScreen<HotbarLociScreenHandler> {
    private static final Identifier BACKGROUND = new Identifier(MOD_ID,"textures/gui/hotbar_loci.png");
    private static final Identifier SELECTED = new Identifier(MOD_ID, "textures/gui/selected.png");
    private final HotbarLociEntity entity;

    public HotbarLociScreen(HotbarLociScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        entity = handler.getInventory();
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
        int centerX = (int) (80*5.89);
        int centerY = (int) (32*6.42);
        Pair<Integer, Integer> vec = switch(entity.getSlot()){
            case 0 -> new Pair<>(centerX-16, centerY-16-4);
            case 1 -> new Pair<>(centerX+16, centerY-16-4);
            case 2 -> new Pair<>(centerX+16+8, centerY);
            case 3 -> new Pair<>(centerX+16, centerY+16+4);
            case 4 -> new Pair<>(centerX-16, centerY+16+4);
            case 5 -> new Pair<>(centerX-16-8, centerY);

            default -> throw new IllegalStateException("Unexpected value: " + entity.getSlot());
        };
        int amount = 1;
        context.drawTexture(SELECTED, vec.getLeft()*amount, vec.getRight()*amount, 0, 0, 18, 18, 18, 18);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(BACKGROUND, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }
}
