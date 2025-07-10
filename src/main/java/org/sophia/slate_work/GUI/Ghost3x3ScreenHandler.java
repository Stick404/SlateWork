package org.sophia.slate_work.GUI;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ClickType;

import java.util.Optional;

import static org.sophia.slate_work.Slate_work.GHOST_3X3_SCREEN;

public class Ghost3x3ScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public Ghost3x3ScreenHandler(int syncId, PlayerInventory inventory, PlayerInventory playerInventory, Inventory blockEntity) {
        this(syncId, playerInventory, new SimpleInventory(9));
    }

    public Ghost3x3ScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(GHOST_3X3_SCREEN, syncId);
        checkSize(inventory, 9);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.addSlot(new GhostSlot(inventory, j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public Ghost3x3ScreenHandler(int id, PlayerInventory playerInventory, PacketByteBuf packetByteBuf) {
        this(id,playerInventory, playerInventory,
                (Inventory) playerInventory.player.getWorld().getBlockEntity(packetByteBuf.readBlockPos()));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (actionType != SlotActionType.CLONE && actionType != SlotActionType.PICKUP) return;
        super.onSlotClick(slotIndex, button, actionType, player);
    }
}
