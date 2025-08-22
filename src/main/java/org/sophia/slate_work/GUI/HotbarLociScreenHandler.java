package org.sophia.slate_work.GUI;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.sophia.slate_work.blocks.entities.HotbarLociEntity;

import static org.sophia.slate_work.Slate_work.HOTBAR_LOCI_SCREEN;

public class HotbarLociScreenHandler extends ScreenHandler {
    private final HotbarLociEntity inventory;

    public HotbarLociEntity getInventory() {
        return inventory;
    }

    public HotbarLociScreenHandler(int syncId, PlayerInventory inventory, PlayerInventory playerInventory, HotbarLociEntity blockEntity) {
        this(syncId, playerInventory, blockEntity);
    }

    public HotbarLociScreenHandler(int id, PlayerInventory playerInventory, PacketByteBuf packetByteBuf) {
        this(id,playerInventory, playerInventory,
                (HotbarLociEntity) playerInventory.player.getWorld().getBlockEntity(packetByteBuf.readBlockPos())
        );
    }

    public HotbarLociScreenHandler(int syncId, PlayerInventory playerInventory, HotbarLociEntity entity) {
        super(HOTBAR_LOCI_SCREEN, syncId);
        this.inventory = entity;
        checkSize(inventory, 6);
        inventory.onOpen(playerInventory.player);

        // 80x, 32y is the center
        int centerX = 80;
        int centerY = 32;
        this.addSlot(new Slot(inventory, 0 , centerX-16, centerY-16-4));
        this.addSlot(new Slot(inventory, 1 , centerX+16, centerY-16-4));
        this.addSlot(new Slot(inventory, 2 , centerX+16+8, centerY));
        this.addSlot(new Slot(inventory, 3 , centerX+16, centerY+16+4));
        this.addSlot(new Slot(inventory, 4 , centerX-16, centerY+16+4));
        this.addSlot(new Slot(inventory, 5 , centerX-16-8, centerY));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        // Gotten from the Shulker Box, no clue if it works...
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = this.slots.get(slot);
        if (slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            if (slot < this.inventory.size() ? !this.insertItem(itemStack2, this.inventory.size(), this.slots.size(), true) : !this.insertItem(itemStack2, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY);
            } else {
                slot2.markDirty();
            }
        }
        return itemStack;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        super.onSlotClick(slotIndex, button, actionType, player);
        if (!player.getWorld().isClient()) inventory.sync();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true; // I dont care any more
    }
}
