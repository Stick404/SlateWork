package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.StorageLociEntity;
import org.sophia.slate_work.registries.BlockRegistry;

import static at.petrak.hexcasting.common.blocks.circles.BlockSlate.*;

@SuppressWarnings("deprecation")
public class StorageLoci extends AbstractSlate implements Equipment, BlockEntityProvider {

    // Hell!
    // This was Hell to make. All of these *hand made*
    private static final VoxelShape DOWN_AB = VoxelShapes.union(AABB_FLOOR,
            createCuboidShape(2,1,2,14,4,14),
            createCuboidShape(4,4,4,12,7,12),
            createCuboidShape(2,7,2,14,10,14));
    private static final VoxelShape UP_AB = VoxelShapes.union(AABB_CEILING,
            createCuboidShape(2,12,2,14,15,14),
            createCuboidShape(4,9,4,12,12,12),
            createCuboidShape(2,6,2,14,9,14));
    private static final VoxelShape EAST_AB = VoxelShapes.union(AABB_EAST_WALL,
            createCuboidShape(1,2,2,4,14,14),
            createCuboidShape(4,4,4,7,12,12),
            createCuboidShape(7,2,2,10,14,14));
    private static final VoxelShape WEST_AB = VoxelShapes.union(AABB_WEST_WALL,
            createCuboidShape(12,2,2,15,14,14),
            createCuboidShape(9,4,4,15,12,12),
            createCuboidShape(6,2,2,9,14,14));
    private static final VoxelShape NORTH_AB = VoxelShapes.union(AABB_NORTH_WALL,
            createCuboidShape(2,2,12,14,14,15),
            createCuboidShape(4,4,9,12,12,15),
            createCuboidShape(2,2,6,14,14,9));
    private static final VoxelShape SOUTH_AB = VoxelShapes.union(AABB_SOUTH_WALL,
            createCuboidShape(2,2,1,14,14,4),
            createCuboidShape(4,4,4,12,12,7),
            createCuboidShape(2,2,7,14,14,10));

    public StorageLoci(Settings p_53182_) {
        super(p_53182_);
        this.setDefaultState(this.stateManager.getDefaultState().with(ENERGIZED, false).with(FACING, Direction.NORTH).with(WATERLOGGED, false));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
    }

    @Override
    public float particleHeight(BlockPos pos, BlockState bs, World world) {
        return 0.25f;
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
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
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
    public @Nullable BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new StorageLociEntity(pPos,pState);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof StorageLociEntity storageLoci && newState.isAir()) {
            if (!world.isClient && !storageLoci.isEmpty()) {
                ItemStack itemStack = new ItemStack(BlockRegistry.STORAGE_LOCI);
                blockEntity.setStackNbt(itemStack);
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
                world.setBlockState(pos,Blocks.AIR.getDefaultState());
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public EquipmentSlot getSlotType() { //hehe, silly hat
        return EquipmentSlot.HEAD;
    }
}
