package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.misc.MediaConstants;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.SentinelLociEntity;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleMedia;

import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.*;

public class SentinelLoci extends AbstractSlate implements BlockEntityProvider, Equipment {
    public SentinelLoci(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SentinelLociEntity(pos, state);
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage castingImage, CircleCastEnv circleCastEnv, Direction direction, BlockPos blockPos, BlockState blockState, ServerWorld serverWorld) {
        var exitDirsSet = this.possibleExitDirections(blockPos, blockState, serverWorld);
        exitDirsSet.remove(direction.getOpposite());
        var exitDirs = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(blockPos, dir));
        var data = castingImage.getUserData().copy();

        if (!blockState.get(ENERGIZED)) {
            var extracted = circleCastEnv.extractMedia(MediaConstants.DUST_UNIT / 100, false);
            if (0L != extracted) {
                this.fakeThrowMishap(
                        blockPos, blockState, castingImage, circleCastEnv,
                        new MishapSpellCircleMedia(MediaConstants.DUST_UNIT / 100, blockPos)
                );
                return new ControlFlow.Stop();
            }
        }

        var sentTime = data.getLong("sentinel_time");
        if (sentTime == 0L){
            // To make sure this is never 0
            data.putLong("sentinel_time", circleCastEnv.getWorld().getTime());
        }

        var entity = serverWorld.getBlockEntity(blockPos);
        if (entity instanceof SentinelLociEntity sent){
            var list = data.getList("sentinel_loci", NbtElement.COMPOUND_TYPE);
            var compound = new NbtCompound();
            compound.put("pos",NbtHelper.fromBlockPos(sent.getPos()));
            compound.putLong("count", 0);
            list.add(compound);
            data.put("sentinel_loci",list);
            return new ControlFlow.Continue(
                    castingImage.copy(castingImage.getStack(),castingImage.getParenCount(),castingImage.getParenthesized(),
                            castingImage.getEscapeNext(),castingImage.getOpsConsumed(),data), exitDirs.toList()
            );
        }

        return new ControlFlow.Stop();
    }

    private static final VoxelShape UP_AB = VoxelShapes.union(AABB_FLOOR,
            createCuboidShape(6,1,6,10,7,10),
            createCuboidShape(1,7,1,15,9,15));
    private static final VoxelShape DOWN_AB = VoxelShapes.union(AABB_CEILING,
            createCuboidShape(6,16-7,6,10,16-1,10),
            createCuboidShape(1,16-9,1,15,16-7,15));
    private static final VoxelShape EAST_AB = VoxelShapes.union(AABB_EAST_WALL,
            createCuboidShape(1,6,6,7,10,9),
            createCuboidShape(7,1,1,9,15,15));
    private static final VoxelShape WEST_AB = VoxelShapes.union(AABB_WEST_WALL,
            createCuboidShape(16-7,6,6,16-1,10,9),
            createCuboidShape(16-9,1,1,16-7,15,15));
    private static final VoxelShape SOUTH_AB = VoxelShapes.union(AABB_SOUTH_WALL,
            createCuboidShape(6,6,1,9,10,7),
            createCuboidShape(1,1,7,15,15,9));
    private static final VoxelShape NORTH_AB = VoxelShapes.union(AABB_NORTH_WALL,
            createCuboidShape(6,6,16-7,9,10,16-1),
            createCuboidShape(1,1,16-9,15,15,16-7));

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case DOWN -> DOWN_AB;
            case UP -> UP_AB;
            case NORTH -> NORTH_AB;
            case SOUTH -> SOUTH_AB;
            case WEST -> WEST_AB;
            case EAST -> EAST_AB;
        };
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }
}
