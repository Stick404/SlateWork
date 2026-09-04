package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.BlockBreakLociEntity;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleMedia;
import org.sophia.slate_work.misc.CircleHelper;
import org.sophia.slate_work.registries.BlockRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockBreakLoci extends AbstractSlate implements BlockEntityProvider {
    private static final double THICKNESS = 4;
    private static final VoxelShape DOWN_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, 16, THICKNESS, 16),
            Block.createCuboidShape(3,3,3,13,9,13));
    private static final VoxelShape UP_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 16 - THICKNESS, 0, 16, 16, 16),
            Block.createCuboidShape(3,7,3,13,13,13));
    private static final VoxelShape EAST_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, THICKNESS, 16, 16),
            Block.createCuboidShape(7,3,3,9,13,13));
    private static final VoxelShape WEST_AB = VoxelShapes.union(
            Block.createCuboidShape(16 - THICKNESS, 0, 0, 16, 16, 16),
            Block.createCuboidShape(7,3,3,13,13,13));
    private static final VoxelShape NORTH_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 16 - THICKNESS, 16, 16, 16),
            Block.createCuboidShape(3,3,7,13,13,13));
    private static final VoxelShape SOUTH_AB = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, 16, 16, THICKNESS),
            Block.createCuboidShape(3,3,3,13,13,9));


    public BlockBreakLoci(Settings p_49795_) {
        super(p_49795_);
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
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        ArrayList<Iota> stack = new ArrayList<>(imageIn.getStack());
        var exitDirsSet = this.possibleExitDirections(pos, bs, world);
        exitDirsSet.remove(enterDir.getOpposite());
        var exits = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(pos, dir)).toList();

        BlockEntity entity = world.getBlockEntity(pos);
        if (!(entity instanceof BlockBreakLociEntity loci)){
            return new ICircleComponent.ControlFlow.Stop();
        }

        ItemStack fakePick = new ItemStack(Items.NETHERITE_PICKAXE);
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.fromNbt(loci.getEnchantments());

        EnchantmentHelper.set(enchantments, fakePick);

        Direction facing = bs.get(FACING);
        BlockPos targetPos = pos.add(facing.getVector());
        BlockState targetBlock = world.getBlockState(targetPos);
        BlockEntity targetEntity = world.getBlockEntity(targetPos);

        if (!(targetBlock.getHardness(world, pos) >= 0f
                && !targetBlock.isAir()
                && IXplatAbstractions.INSTANCE.isCorrectTierForDrops(ToolMaterials.NETHERITE, targetBlock)
                && IXplatAbstractions.INSTANCE.isBreakingAllowed(world, pos, targetBlock, env.getCaster()))) {

            return new ICircleComponent.ControlFlow.Continue(imageIn.copy(stack, imageIn.getParenCount(),
                    imageIn.getParenthesized(), imageIn.getEscapeNext(), imageIn.getOpsConsumed(), imageIn.getUserData()), exits);

        }

        boolean isCheap = world.getBlockState(pos).streamTags().anyMatch(a -> a.equals(HexTags.Blocks.CHEAP_TO_BREAK_BLOCK));
        long cheapCost = isCheap ? MediaConstants.DUST_UNIT / 100 : MediaConstants.DUST_UNIT / 8;

        Integer fortuneCostI = enchantments.get(Enchantments.FORTUNE);
        long fortuneCost = 0;
        if (fortuneCostI != null) { // Since the item may not have Fortune
            fortuneCost = fortuneCostI.longValue();
        }


        long silkTouchCost = 0;
        if (enchantments.containsKey(Enchantments.SILK_TOUCH)) { // Nor SilkTouch
            silkTouchCost = 1;
        }

        Integer efficiencyMultI = enchantments.get(Enchantments.EFFICIENCY);
        long efficiencyMult = 2;
        if (efficiencyMultI != null) { // Since the item may not have Fortune
            efficiencyMult = efficiencyMultI.longValue() +2;
        }

        // Uhh, random bullshit go
        long cost = (long) ((cheapCost + (silkTouchCost*MediaConstants.SHARD_UNIT) + (fortuneCost*MediaConstants.DUST_UNIT))/(efficiencyMult*0.5));
        // Okay so. Theres the source cost, then its added by either Silk Touch (an extra shard), or added Fortune*a shard.
        // This means Fortune 3 (highest base game) is an extra 3 shards; feels fare since its a locus/needs the block in front of it.
        // Then, the whole cost is divided by efficiency*0.5; meaning highest cost reduce (base game) is 2.5 times.
        // So, the "best" locus would cost... 1.25 dust (Eff 5, Fort 3)

        var extracted = env.extractMedia(cost, false);
        if (0L != extracted) {
            this.fakeThrowMishap(
                    pos, bs, imageIn, env,
                    new MishapSpellCircleMedia(extracted, pos)
            );
            return new ControlFlow.Stop();
        }

        Vec3d center = targetPos.toCenterPos();

        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.25f, 0.6f);
        world.getPlayers().forEach(a -> a.networkHandler.sendPacket(new ParticleS2CPacket(
                ParticleTypes.EXPLOSION, false, center.getX(), center.getY(), center.getZ(), 0, 0, 0, 0, 1
        )));
        List<ItemStack> droppedItems = Block.getDroppedStacks(targetBlock, world, pos, targetEntity, env.getCastingEntity(), fakePick);
        world.breakBlock(targetPos, false);

        for (ItemStack item : droppedItems){
            if (!CircleHelper.INSTANCE.storeItems(env, item)){
                // If it fails to store, spit item out
                ItemEntity itemEntity = new ItemEntity(world, center.getX(), center.getY(), center.getZ(), item);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }


        return new ICircleComponent.ControlFlow.Continue(imageIn.copy(stack, imageIn.getParenCount(),
                imageIn.getParenthesized(), imageIn.getEscapeNext(), imageIn.getOpsConsumed(), imageIn.getUserData()), exits);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlockBreakLociEntity(pos, state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.getBlockEntity(pos) instanceof BlockBreakLociEntity loci && !world.isClient){
            loci.setEnchantments(itemStack.getEnchantments());
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BlockBreakLociEntity loci && !newState.isOf(state.getBlock())) {
            if (!world.isClient) {
                ItemStack itemStack = new ItemStack(BlockRegistry.BLOCK_BREAKING_LOCI_ITEM);

                var enchants = EnchantmentHelper.fromNbt(loci.getEnchantments());
                for (var enchant : enchants.entrySet()){
                    itemStack.addEnchantment(enchant.getKey(), enchant.getValue());
                }

                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
