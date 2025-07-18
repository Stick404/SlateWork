package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.MacroLociEntity;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleReadableFocus;

public class MacroLoci extends AbstractSlate implements BlockEntityProvider {
    public static final BooleanProperty FOCUS = BooleanProperty.of("focus"); // Sure, the Locus is an EYE
    public MacroLoci(Settings p_49795_) {
        super(p_49795_);
        this.setDefaultState(this.stateManager.getDefaultState().with(FOCUS,false));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FOCUS);
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
            if (!(holder.readIota(serverWorld) instanceof ListIota)){
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
                if (compound.get("pattern") == pattern){
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
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        var entity = world.getBlockEntity(pos);
        if (entity instanceof MacroLociEntity loci){
            if (!loci.getStack(0).isEmpty()){
                world.spawnEntity(new ItemEntity(world,
                        pos.getX(), pos.getY(),  pos.getZ(), loci.getStack(0)));
            }
        }
        super.onBreak(world, pos, state, player);
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
                    player.setStackInHand(hand,ItemStack.EMPTY); // and clear the player's hand
                    loci.markDirty();
                }
            } else { // if the Loci has a focus in it already...
                ItemStack installed = loci.removeStack(0).copy();
                ItemStack held = player.getStackInHand(hand);

                if (loci.isValid(0,held)){ // If the item is valid...
                    loci.setStack(0,held); // Set the stack of the Loci
                    player.setStackInHand(hand,ItemStack.EMPTY); // and clear the player's hand
                    loci.markDirty();
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
