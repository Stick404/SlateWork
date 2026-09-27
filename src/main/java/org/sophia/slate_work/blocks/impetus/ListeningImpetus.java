package org.sophia.slate_work.blocks.impetus;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
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
import org.sophia.slate_work.registries.SlateWorksBlockRegistry;
import org.sophia.slate_work.saving.Listeners;
import ram.talia.moreiotas.api.casting.iota.StringIota;

public class ListeningImpetus extends BlockAbstractImpetus {

    public ListeningImpetus(Settings p_49795_) {
        super(p_49795_);
    }

    @Override
    public BlockEntityType<? extends BlockEntityAbstractImpetus> getBlockEntityType() {
        return SlateWorksBlockRegistry.LISTENING_IMPETUS_ENTITY;
    }

    @Override
    public ActionResult use(BlockState pState, World world, BlockPos pos, PlayerEntity player, Hand pHand, BlockHitResult pHit) {
        if (world instanceof ServerWorld sLevel && sLevel.getBlockEntity(pos) instanceof ListeningImpetusEntity entity){
            var usedStack = player.getStackInHand(player.getActiveHand());
            if (usedStack.isEmpty() && player.isSneaking()){
                entity.clear();
                entity.sync();
                sLevel.playSound(null, pos, HexSounds.IMPETUS_REDSTONE_CLEAR.value(), SoundCategory.BLOCKS, 1f, 1f);
                return ActionResult.SUCCESS;
            } else {
                var datumItem = IXplatAbstractions.INSTANCE.findDataHolder(usedStack);
                if (datumItem != null){
                    var data = datumItem.readIota();
                    if (data instanceof StringIota text){
                        entity.setString(text.getString());
                        entity.sync();
                        sLevel.playSound(null, pos, HexSounds.IMPETUS_REDSTONE_DING.value(), SoundCategory.BLOCKS, 1f, 1f);
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }
        return ActionResult.PASS;
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
}
