package org.sophia.slate_work.GUI;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.sophia.slate_work.blocks.CraftingLoci;
import org.sophia.slate_work.blocks.entities.CraftingLociEntity;
import org.sophia.slate_work.misc.DumbDumbInv;

import static org.sophia.slate_work.Slate_work.GHOST_3X3_SCREEN;

public class Ghost3x3ScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final World world;

    public Ghost3x3ScreenHandler(int syncId, PlayerInventory inventory, PlayerInventory playerInventory, Inventory blockEntity) {
        this(syncId, playerInventory, blockEntity);
    }

    public Ghost3x3ScreenHandler(int id, PlayerInventory playerInventory, PacketByteBuf packetByteBuf) {
        this(id,playerInventory, playerInventory,
                new DumbDumbInv((CraftingLociEntity) playerInventory.player.getWorld().getBlockEntity(packetByteBuf.readBlockPos()))
        );
    }

    public Ghost3x3ScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(GHOST_3X3_SCREEN, syncId);
        checkSize(inventory, 9);
        this.inventory = inventory;
        this.world = playerInventory.player.getWorld();
        inventory.onOpen(playerInventory.player);

        this.addSlot(new GhostSlotOutput(inventory,9,138,32,this));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.addSlot(new GhostSlot(inventory, j + i * 3, 62 + j * 18, 14 + i * 18));
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

    @Override
    public void onContentChanged(Inventory inventory) {
        Ghost3x3ScreenHandler.updateRecipe(this.world,inventory);
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
        if ((actionType != SlotActionType.CLONE && actionType != SlotActionType.PICKUP && actionType != SlotActionType.QUICK_MOVE) || button == -1) return;

        if (slotIndex < 10 && slotIndex > 0){ // Ok cool, we are working in the Ghost Screen
            // If we are in creative mode, copy the selected items
            if (actionType == SlotActionType.CLONE && player.getAbilities().creativeMode && this.getCursorStack().isEmpty()) {
                Slot slot3 = this.slots.get(slotIndex);
                if (slot3.hasStack()) {
                    ItemStack itemStack2 = slot3.getStack();
                    this.setCursorStack(itemStack2.copyWithCount(itemStack2.getMaxCount()));
                }
            //else, just act as normal
            } else {
                var copy = this.getCursorStack().copy();
                copy.setCount(1);
                this.inventory.setStack(slotIndex-1,copy);
                Ghost3x3ScreenHandler.updateRecipe(this.world,inventory);
            }
        } else super.onSlotClick(slotIndex, button, actionType, player);
        /*
        if (!(this.getSlot(slotIndex).inventory instanceof PlayerInventory) || slotIndex > 0 || slotIndex < 10  || (button == 0 || button == 1)) {
            var copy = this.getCursorStack().copy();
            copy.setCount(1);
            this.inventory.setStack(slotIndex -2,copy);
        } else {
            super.onSlotClick(slotIndex, button, actionType, player);
        }
        */
    }

    public static void updateRecipe(World world, Inventory inventory){
        if (inventory instanceof IGhostCrafting ghostCrafting){
            var container = new CraftingInventory(new CraftingLoci.AutocraftingMenu(), 3, 3);
            for (int i = 0; i < 9; i++){
                container.setStack(i,ghostCrafting.getStack(i));
            }

            var recipeOpt = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, container ,world);
            if (recipeOpt.isEmpty()){
                ghostCrafting.setStack(9,ItemStack.EMPTY);
                return;
            }

            var outputItem = recipeOpt.get().craft(container,world.getRegistryManager());
            ghostCrafting.setCraftSlot(outputItem);
        }
    }
}
