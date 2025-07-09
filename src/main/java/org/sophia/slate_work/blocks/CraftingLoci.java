package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.CraftingLociEntity;
import org.sophia.slate_work.casting.mishap.MishapNoJars;
import org.sophia.slate_work.misc.CircleHelper;

public class CraftingLoci extends AbstractSlate implements BlockEntityProvider {

    public CraftingLoci(Settings p_49795_) {
        super(p_49795_);
    }

    @Override
    public ICircleComponent.ControlFlow acceptControlFlow(CastingImage castingImage, CircleCastEnv circleCastEnv, Direction direction, BlockPos blockPos, BlockState blockState, ServerWorld serverWorld) {
        BlockEntity entity = serverWorld.getBlockEntity(blockPos);
        if (entity instanceof CraftingLociEntity craftingLoci){
            var stack = castingImage.getStack();

            if (stack.isEmpty()) { // Feels silly, but this is what Hex does
                this.fakeThrowMishap(
                        blockPos, blockState, castingImage, circleCastEnv,
                        new MishapNotEnoughArgs(1,0)
                );
                return new ControlFlow.Stop();
            }
            var storages = CircleHelper.INSTANCE.getStorage(circleCastEnv);
            if (storages.isEmpty()) {
                this.fakeThrowMishap(
                        blockPos, blockState, castingImage, circleCastEnv,
                        new MishapNoJars()
                );
                return new ControlFlow.Stop();
            } // Ok cool, theres an iota, and we have jars





        } else {
            return new ControlFlow.Stop();
        }
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
}
