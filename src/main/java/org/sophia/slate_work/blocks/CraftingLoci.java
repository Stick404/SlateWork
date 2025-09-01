package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.CraftingLociEntity;
import org.sophia.slate_work.casting.mishap.MishapNoStorageLoci;
import org.sophia.slate_work.misc.CircleHelper;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static at.petrak.hexcasting.common.lib.HexSounds.IMPETUS_REDSTONE_DING;

@SuppressWarnings({"deprecation", "UnstableApiUsage"})
public class CraftingLoci extends BlockCircleComponent implements BlockEntityProvider, Equipment {

    public CraftingLoci(Settings p_49795_) {
        super(p_49795_);
        this.setDefaultState(this.stateManager.getDefaultState().with(ENERGIZED, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.union(
                // Foundation
                createCuboidShape(0,0,0,16,2,2),
                createCuboidShape(0,0,0,2,2,16),
                createCuboidShape(14,0,0,16,2,16),
                createCuboidShape(0,0,14,16,2,16),

                // Vertical bars
                createCuboidShape(7,0,0,9,14,2),
                createCuboidShape(7,0,14,9,14,16),
                createCuboidShape(0,0,7,2,14,9),
                createCuboidShape(14,0,7,16,14,9),

                // Center Cube + center bars
                createCuboidShape(5,4,5,11,10,11),
                createCuboidShape(0,6,7,16,8,9),
                createCuboidShape(7,6,0,9,8,16),

                // Top slate
                createCuboidShape(0,12,0,16,14,16)
        );
    }

    @Override
    public Direction normalDir(BlockPos blockPos, BlockState blockState, World world, int i) {
        return Direction.UP;
    }

    @Override
    public float particleHeight(BlockPos blockPos, BlockState blockState, World world) {
        return 0.5f;
    }
    @Override
    public boolean canEnterFromDirection(Direction direction, BlockPos blockPos, BlockState blockState, ServerWorld serverWorld) {
        return direction != Direction.UP;
    }

    @Override
    public EnumSet<Direction> possibleExitDirections(BlockPos blockPos, BlockState blockState, World world) {
        EnumSet<Direction> z = EnumSet.allOf(Direction.class);
        z.remove(Direction.UP);
        return z;
    }

    @Override
    public ICircleComponent.ControlFlow acceptControlFlow(CastingImage castingImage, CircleCastEnv circleCastEnv, Direction direction, BlockPos blockPos, BlockState blockState, ServerWorld serverWorld) {
        BlockEntity entity = serverWorld.getBlockEntity(blockPos);

        if (entity instanceof CraftingLociEntity craftingLoci) {
            ArrayList<Iota> stack = new ArrayList<>(castingImage.getStack());
            var exitDirsSet = this.possibleExitDirections(blockPos, blockState, serverWorld);
            exitDirsSet.remove(direction.getOpposite());
            var exits = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(blockPos, dir)).toList();

            var storages = CircleHelper.INSTANCE.getLists(circleCastEnv);
            if (storages.isEmpty()) {
                this.fakeThrowMishap(
                        blockPos, blockState, castingImage, circleCastEnv,
                        new MishapNoStorageLoci(blockPos)
                );
                return new ControlFlow.Stop();
            }

            var entities = CircleHelper.INSTANCE.getStorage(circleCastEnv);
            if (entities.size() * 16 <= storages.size()) { // Woops! No storage
                stack.add(new BooleanIota(false));
                return new ControlFlow.Continue(
                        castingImage.copy(stack, castingImage.getParenCount(), castingImage.getParenthesized(), castingImage.getEscapeNext(), castingImage.getOpsConsumed(), castingImage.getUserData()),
                        exits);
            }

            // Idk mate, this is what Hexal Does
            var container = new CraftingInventory(new AutocraftingMenu(), 3, 3);
            Map<ItemVariant, Integer> shoppingList = new HashMap<>();
            for (int i = 0; i < 9; i++) {
                var temp = craftingLoci.getStack(i);
                ItemVariant variant = ItemVariant.of(temp);
                if (!storages.containsKey(variant)) { // If we cant find the variant, kill the search and push false
                    stack.add(new BooleanIota(false));
                    return new ControlFlow.Continue(
                            castingImage.copy(stack, castingImage.getParenCount(), castingImage.getParenthesized(), castingImage.getEscapeNext(), castingImage.getOpsConsumed(), castingImage.getUserData()),
                            exits);
                }

                // If we know the item is there, increment it, else, add it
                if (shoppingList.containsKey(variant)) shoppingList.put(variant, shoppingList.get(variant) + 1);
                else shoppingList.put(variant, 1);
                container.setStack(i, temp);
            }

            var recipeOpt = serverWorld.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, container, serverWorld);

            if (recipeOpt.isEmpty()) { // If a recipe was not found, then yadadada
                stack.add(new BooleanIota(false));
                return new ControlFlow.Continue(
                        castingImage.copy(stack, castingImage.getParenCount(), castingImage.getParenthesized(), castingImage.getEscapeNext(), castingImage.getOpsConsumed(), castingImage.getUserData()),
                        exits);
            }

            for (var pair : shoppingList.entrySet()) { // Here we check if what we want is less than what we have
                if (pair.getKey().isBlank())
                    continue;
                var slot = storages.get(pair.getKey());
                if (slot.getCount() < pair.getValue()) { // If so, kill and push false
                    stack.add(new BooleanIota(false));
                    return new ControlFlow.Continue(
                            castingImage.copy(stack, castingImage.getParenCount(), castingImage.getParenthesized(), castingImage.getEscapeNext(), castingImage.getOpsConsumed(), castingImage.getUserData()),
                            exits);
                } else {
                            slot.getStorageLociEntity().removeStack(
                            slot.getStorageLociEntity().getSlot(slot.getItem()),
                            pair.getValue());
                }
            }

            var outputItem = recipeOpt.get().craft(container, serverWorld.getRegistryManager());
            var remainderItems = recipeOpt.get().getRemainder(container);

            CircleHelper.INSTANCE.storeItems(circleCastEnv, outputItem);
            for (var item : remainderItems) {
                CircleHelper.INSTANCE.storeItems(circleCastEnv, item);
            }

            serverWorld.playSound(null, blockPos, IMPETUS_REDSTONE_DING, SoundCategory.BLOCKS, 1.0F, 1F);
            stack.add(new BooleanIota(true));
            return new ControlFlow.Continue(
                    castingImage.copy(stack, castingImage.getParenCount(), castingImage.getParenthesized(), castingImage.getEscapeNext(), castingImage.getOpsConsumed(), castingImage.getUserData()),
                    exits);
        } else {
            return new ControlFlow.Stop();
        }
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

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }

    // Ok so, no clue why Hexal does it this way, but we are just going to copy what it does (and Hexal copies AE2 lmao)
    // https://github.com/Talia-12/Hexal/blob/main/Common/src/main/java/ram/talia/hexal/common/casting/actions/spells/motes/OpCraftMote.kt
    public static class AutocraftingMenu extends ScreenHandler {
        public AutocraftingMenu() {super(null, 0);}
        @Override
        public ItemStack quickMove(PlayerEntity player, int slot) {return ItemStack.EMPTY;}
        @Override
        public boolean canUse(PlayerEntity player) {return false;}
    }
}
