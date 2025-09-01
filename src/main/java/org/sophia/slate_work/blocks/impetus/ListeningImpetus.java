package org.sophia.slate_work.blocks.impetus;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import miyucomics.hexpose.iotas.TextIota;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.saving.Listeners;

public class ListeningImpetus extends BlockAbstractImpetus {

    public ListeningImpetus(Settings p_49795_) {
        super(p_49795_);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient){
            if (world.getBlockEntity(pos) instanceof ListeningImpetusEntity entity) {
                Listeners.saveListener((ServerWorld) world, pos);
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onStateReplaced(pState, pLevel, pPos, pNewState, pIsMoving);
        if (!pLevel.isClient && !pState.isOf(pNewState.getBlock())){
            Listeners.removeListener((ServerWorld) pLevel, pPos);
        }
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ListeningImpetusEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world instanceof ServerWorld sLevel && sLevel.getBlockEntity(pos) instanceof ListeningImpetusEntity entity){
            var usedStack = player.getStackInHand(hand);
            if (usedStack.isEmpty() && player.isSneaking()){
                entity.clear();
                entity.sync();
                sLevel.playSound(null, pos, HexSounds.IMPETUS_REDSTONE_CLEAR, SoundCategory.BLOCKS, 1f, 1f);
                return ActionResult.SUCCESS;
            } else {
                var datumItem = IXplatAbstractions.INSTANCE.findDataHolder(usedStack);
                if (datumItem != null){
                    var data = datumItem.readIota(sLevel);
                    if (data instanceof TextIota text){
                        entity.setString(text.getText().getString());
                        entity.sync();
                        sLevel.playSound(null, pos, HexSounds.IMPETUS_REDSTONE_DING, SoundCategory.BLOCKS, 1f, 1f);
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }
        return ActionResult.PASS;
    }
}
