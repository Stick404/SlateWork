package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class StorageLoci extends BlockSlate {
    public StorageLoci(Settings p_53182_) {
        super(p_53182_);
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        var exitDirsSet = this.possibleExitDirections(pos, bs, world);
        exitDirsSet.remove(enterDir.getOpposite());
        var exitDirs = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(pos, dir));
        var data = imageIn.getUserData().copy();
        var list = data.getList("storage_loci", NbtElement.COMPOUND_TYPE);
        var check = NbtHelper.fromBlockPos(pos);
        if (!list.contains(check)) list.add(check);

        data.put("storage_loci",list);

        return new ControlFlow.Continue(imageIn.copy(imageIn.getStack(),imageIn.getParenCount(),
                imageIn.getParenthesized(),imageIn.getEscapeNext(), imageIn.getOpsConsumed(), data), exitDirs.toList());
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new StorageLociEntity(pPos,pState);
    }
}
