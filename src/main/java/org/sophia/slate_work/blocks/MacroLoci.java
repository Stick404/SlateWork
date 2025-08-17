package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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
import org.sophia.slate_work.blocks.entities.MacroLociEntity;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleReadableFocus;

import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.*;
import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.AABB_NORTH_WALL;
import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.AABB_SOUTH_WALL;
import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.AABB_WEST_WALL;

@SuppressWarnings("deprecation")
public class MacroLoci extends AbstractSlate implements BlockEntityProvider {

    private static final VoxelShape DOWN_AB = VoxelShapes.union(AABB_FLOOR,
            BlockSlate.createCuboidShape(2,1,2,14,4,14));
    private static final VoxelShape UP_AB = VoxelShapes.union(AABB_CEILING,
            BlockSlate.createCuboidShape(2,12,2,14,15,14));
    private static final VoxelShape EAST_AB = VoxelShapes.union(AABB_EAST_WALL,
            BlockSlate.createCuboidShape(1,2,2,4,14,14));
    private static final VoxelShape WEST_AB = VoxelShapes.union(AABB_WEST_WALL,
            BlockSlate.createCuboidShape(12,2,2,15,14,14));
    private static final VoxelShape NORTH_AB = VoxelShapes.union(AABB_NORTH_WALL,
            BlockSlate.createCuboidShape(2,2,12,14,14,15));
    private static final VoxelShape SOUTH_AB = VoxelShapes.union(AABB_SOUTH_WALL,
            BlockSlate.createCuboidShape(2,2,1,14,14,4));

    public static final BooleanProperty FOCUS = BooleanProperty.of("focus");
    public MacroLoci(Settings p_49795_) {
        super(p_49795_);
        this.setDefaultState(this.stateManager.getDefaultState().with(FOCUS,false).with(ENERGIZED, false));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FOCUS);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return switch (pState.get(FACING)){
            case NORTH -> NORTH_AB;
            case SOUTH -> SOUTH_AB;
            case WEST -> WEST_AB;
            case EAST -> EAST_AB;
            case UP -> DOWN_AB;
            case DOWN -> UP_AB;
        };
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage castingImage, CircleCastEnv circleCastEnv, Direction direction, BlockPos blockPos, BlockState blockState, ServerWorld serverWorld) {
        var exitDirsSet = this.possibleExitDirections(blockPos, blockState, serverWorld);
        exitDirsSet.remove(direction.getOpposite());
        var exitDirs = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(blockPos, dir));
        var data = castingImage.getUserData().copy();

        if (serverWorld.getBlockEntity(blockPos) instanceof MacroLociEntity loci) {
            NbtCompound macro = new NbtCompound();
            ADIotaHolder holder = IXplatAbstractions.INSTANCE.findDataHolder(loci.getStack(0));

            if (holder == null){
                this.fakeThrowMishap(blockPos, blockState, castingImage, circleCastEnv,
                    new MishapSpellCircleReadableFocus(blockPos));
                return new ControlFlow.Stop();
            }
            if (holder.readIota(serverWorld) == null){
                this.fakeThrowMishap(blockPos, blockState, castingImage, circleCastEnv,
                    new MishapSpellCircleReadableFocus(blockPos));
                return new ControlFlow.Stop();
            }

           if (!(holder.readIota(serverWorld) instanceof ListIota || holder.readIota(serverWorld).executable())){
                this.fakeThrowMishap(blockPos, blockState, castingImage, circleCastEnv,
                        new MishapSpellCircleReadableFocus(blockPos));
                return new ControlFlow.Stop();
            }

            macro.put("macro", holder.readIotaTag());
            var pattern = loci.getPattern().serializeToNBT();
            macro.put("pattern", pattern);

            var macros = data.getList("macros", NbtElement.COMPOUND_TYPE);
            int i = 0;
            for (var z : macros){
                NbtCompound compound = (NbtCompound) z;
                if (PatternIota.deserialize(compound.get("pattern")).getPattern().anglesSignature().equals(loci.getPattern().anglesSignature())){
                    macros.remove(i);
                    break;
                }
                i++;
            }
            macros.add(macro);
            data.put("macros", macros);
        }
        return new ControlFlow.Continue(
                castingImage.copy(castingImage.getStack(), castingImage.getParenCount(), castingImage.getParenthesized(),
                        castingImage.getEscapeNext(), castingImage.getOpsConsumed(), data),
                exitDirs.toList());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        var entity = world.getBlockEntity(pos);
        if (entity instanceof MacroLociEntity loci && newState.isAir()){
            if (!loci.getStack(0).isEmpty()){
                world.spawnEntity(new ItemEntity(world,
                        pos.getX(), pos.getY(),  pos.getZ(), loci.getStack(0)));
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    // Code mostly gotten from the amazing Sam
    // https://github.com/SamsTheNerd/ducky-periphs/blob/56252d6ab19f612a15ad9010a6e64661889ea4b0/common/src/main/java/com/samsthenerd/duckyperiphs/hexcasting/FocalPortBlockEntity.java#L312
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof MacroLociEntity loci){
            if (loci.isEmpty()){ // If the Loci is empty...
                ItemStack stack = player.getStackInHand(hand);
                if (loci.isValid(0,stack)){ // And the item is valid...
                    loci.setStack(0,stack.copy()); // Set the stack of the Loci
                    world.setBlockState(pos,state.with(FOCUS,true));
                    player.setStackInHand(hand,ItemStack.EMPTY); // and clear the player's hand
                    loci.markDirty();
                }
            } else { // if the Loci has a focus in it already...
                ItemStack installed = loci.removeStack(0).copy();
                ItemStack held = player.getStackInHand(hand);

                if (loci.isValid(0,held)){ // If the item is valid...
                    loci.setStack(0,held); // Set the stack of the Loci
                    world.setBlockState(pos,state.with(FOCUS,true));
                    player.setStackInHand(hand,ItemStack.EMPTY); // and clear the player's hand
                    loci.markDirty();
                } else {
                    world.setBlockState(pos,state.with(FOCUS,false));
                }
                if (!player.getInventory().insertStack(installed)){ // If we *can't* put the old stack back into the player's Inv
                    player.dropItem(installed,false); // Then drop the item
                }
                return ActionResult.success(world.isClient);
            }
        }
        return ActionResult.SUCCESS;
    }


    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MacroLociEntity(pos,state);
    }
}
