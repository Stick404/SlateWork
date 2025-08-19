package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.HotbarLociEntity;

public class HotbarLoci extends AbstractSlate implements BlockEntityProvider {
    public HotbarLoci(Settings p_49795_) {
        super(p_49795_);
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        if (world.getBlockEntity(pos) instanceof HotbarLociEntity){
            var data =imageIn.getUserData().copy();
            data.put("hotbar_loci", NbtHelper.fromBlockPos(pos));
            var exitDirsSet = this.possibleExitDirections(pos, bs, world);
            exitDirsSet.remove(enterDir.getOpposite());
            var exitDirs = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(pos, dir));

            return new ControlFlow.Continue(
                    imageIn.copy(imageIn.getStack(), imageIn.getParenCount(), imageIn.getParenthesized(),
                            imageIn.getEscapeNext(), imageIn.getOpsConsumed(), data), exitDirs.toList());
        }
        return new ControlFlow.Stop();
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HotbarLociEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof HotbarLociEntity loci) {
                player.openHandledScreen(loci);
            }
            return ActionResult.CONSUME;
        }
    }
}
