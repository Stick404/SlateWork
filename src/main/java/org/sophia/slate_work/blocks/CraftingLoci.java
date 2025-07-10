package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.CraftingLociEntity;
import org.sophia.slate_work.casting.mishap.MishapNoJars;
import org.sophia.slate_work.misc.CircleHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static at.petrak.hexcasting.common.lib.HexSounds.IMPETUS_REDSTONE_DING;

public class CraftingLoci extends AbstractSlate implements BlockEntityProvider {

    public CraftingLoci(Settings p_49795_) {
        super(p_49795_);
    }

    @Override
    public ICircleComponent.ControlFlow acceptControlFlow(CastingImage castingImage, CircleCastEnv circleCastEnv, Direction direction, BlockPos blockPos, BlockState blockState, ServerWorld serverWorld) {
        BlockEntity entity = serverWorld.getBlockEntity(blockPos);

        if (entity instanceof CraftingLociEntity craftingLoci){
            ArrayList<Iota> stack = new ArrayList<>(castingImage.getStack());
            ArrayList<Pair<BlockPos, Direction>> exits = new ArrayList<>();
            exits.add(new Pair<>(blockPos.offset(direction),direction));

            var storages = CircleHelper.INSTANCE.getLists(circleCastEnv);
            if (storages.isEmpty()) {
                this.fakeThrowMishap(
                        blockPos, blockState, castingImage, circleCastEnv,
                        new MishapNoJars()
                );
                return new ControlFlow.Stop();
            }

            var entities = CircleHelper.INSTANCE.getStorage(circleCastEnv);
            if (entities.size()*16 <= storages.size()) { // Woops! No storage
                stack.add(new BooleanIota(false));
                return new ControlFlow.Continue(
                        castingImage.copy(stack,castingImage.getParenCount(),castingImage.getParenthesized(),castingImage.getEscapeNext(),castingImage.getOpsConsumed(),castingImage.getUserData()),
                        exits);
            }

            // Idk mate, this is what Hexal Does
            var container = new CraftingInventory(new AutocraftingMenu(), 3, 3);

            int i = 0;
            Map<ItemVariant, Integer> shoppingList = new HashMap<>();
            for (var z : craftingLoci.getInv()){
                var temp = z.copy();
                ItemVariant variant = ItemVariant.of(temp);
                if (!storages.containsKey(variant)){ // If we cant find the variant, kill the search and push false
                    stack.add(new BooleanIota(false));
                    return new ControlFlow.Continue(
                            castingImage.copy(stack,castingImage.getParenCount(),castingImage.getParenthesized(),castingImage.getEscapeNext(),castingImage.getOpsConsumed(),castingImage.getUserData()),
                            exits);
                }

                // If we know the item is there, increment it, else, add it
                if (shoppingList.containsKey(variant)) shoppingList.put(variant, shoppingList.get(variant) +1);
                else shoppingList.put(variant, 1);
                container.setStack(i++,temp);
            }

            var recipeOpt = serverWorld.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, container,serverWorld);

            if (recipeOpt.isEmpty()){ // If a recipe was not found, then yadadada
                stack.add(new BooleanIota(false));
                return new ControlFlow.Continue(
                        castingImage.copy(stack,castingImage.getParenCount(),castingImage.getParenthesized(),castingImage.getEscapeNext(),castingImage.getOpsConsumed(),castingImage.getUserData()),
                        exits);
            }

            for (var pair : shoppingList.entrySet()){ // Here we check if what we want is less than what we have
                if (pair.getKey().isBlank())
                    continue;
                var slot = storages.get(pair.getKey());
                if (slot.getCount() < pair.getValue()) { // If so, kill and push false
                    stack.add(new BooleanIota(false));
                    return new ControlFlow.Continue(
                                castingImage.copy(stack,castingImage.getParenCount(),castingImage.getParenthesized(),castingImage.getEscapeNext(),castingImage.getOpsConsumed(),castingImage.getUserData()),
                                exits);
                }
                else slot.getStorageLociEntity().removeStack(
                        slot.getStorageLociEntity().getSlot(slot.getItem()),
                        pair.getValue());
            }

            //for (var z : shoppingList.entrySet()){ // Ok cool, we know everything must be valid, now decrement the known items
            //    storages.compute(z.getKey(), (k, slot) -> new CircleHelper.ItemSlot(slot.getItem(), slot.getCount() - z.getValue(), slot.getStorageLociEntity()));
            //}

            var outputItem = recipeOpt.get().craft(container,serverWorld.getRegistryManager());

            if (storages.containsKey(ItemVariant.of(outputItem))) { //We had the item already in storage!
                var slot = storages.get(ItemVariant.of(outputItem));
                        var targ = slot.getStorageLociEntity().getSlot(slot.getItem()); // *shouldn't* be null
                        var item = slot.getStorageLociEntity().getStack(targ);
                item.setRight(item.getRight() + outputItem.getCount());
                stack.add(new BooleanIota(true));

                serverWorld.playSound(null, blockPos, IMPETUS_REDSTONE_DING, SoundCategory.BLOCKS, 1.0F, 1F);
                return new ControlFlow.Continue(
                        castingImage.copy(stack,castingImage.getParenCount(),castingImage.getParenthesized(),castingImage.getEscapeNext(),castingImage.getOpsConsumed(),castingImage.getUserData()),
                        exits);
            }
            // If its not a known item yet...
            for (var z : entities){
                var x = z.isFull();
                if (x != -1) {
                    serverWorld.playSound(null, blockPos, IMPETUS_REDSTONE_DING, SoundCategory.BLOCKS, 1.0F, 1F);
                    z.setStack(x, ItemVariant.of(outputItem),outputItem.getCount());
                    stack.add(new BooleanIota(true));
                    return new ControlFlow.Continue(
                            castingImage.copy(stack,castingImage.getParenCount(),castingImage.getParenthesized(),castingImage.getEscapeNext(),castingImage.getOpsConsumed(),castingImage.getUserData()),
                            exits);
                }
            }

        } else {
            return new ControlFlow.Stop();
        }
        // If it gets to here, may :hexxy: help you
        System.out.println("Something with Crafting Loci at ".concat(blockPos.toString()).concat(" went wrong!"));
        return new ControlFlow.Stop();
    }


    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CraftingLociEntity(pos,state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CraftingLociEntity loci) {
                player.openHandledScreen(loci);
            }
            return ActionResult.CONSUME;
        }
    }

    // Ok so, no clue why Hexal does it this way, but we are just going to copy what it does (and Hexal copies AE2 lmao)
    // https://github.com/Talia-12/Hexal/blob/main/Common/src/main/java/ram/talia/hexal/common/casting/actions/spells/motes/OpCraftMote.kt
    private static class AutocraftingMenu extends ScreenHandler {
        protected AutocraftingMenu() {super(null, 0);}
        @Override
        public ItemStack quickMove(PlayerEntity player, int slot) {return ItemStack.EMPTY;}
        @Override
        public boolean canUse(PlayerEntity player) {return false;}
    }
}
