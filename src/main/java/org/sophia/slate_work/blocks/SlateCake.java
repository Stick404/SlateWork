package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

import java.util.EnumSet;

import static at.petrak.hexcasting.api.block.circle.BlockCircleComponent.ENERGIZED;
import static org.sophia.slate_work.blocks.AbstractSlate.FACING;
import static org.sophia.slate_work.blocks.AbstractSlate.WATERLOGGED;

public class SlateCake extends CakeBlock implements ICircleComponent {
    public static final IntProperty SLATE_BITES = IntProperty.of("bites", 0, 14);

    public SlateCake(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(SLATE_BITES, 0).with(ENERGIZED, false).with(FACING, Direction.UP).with(WATERLOGGED, false));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        //super.appendProperties(builder);
        builder.add(FACING, WATERLOGGED, ENERGIZED, SLATE_BITES, BITES);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            if (tryEat(world, pos, state, player).isAccepted()) {
                return ActionResult.SUCCESS;
            }
        }

        return tryEat(world, pos, state, player);
    }

    protected static ActionResult tryEat(WorldAccess world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!player.canConsume(false)) {
            return ActionResult.PASS;
        } else {
            player.incrementStat(Stats.EAT_CAKE_SLICE);
            player.getHungerManager().add(2, 0.1F);
            int i = state.get(SLATE_BITES);
            world.emitGameEvent(player, GameEvent.EAT, pos);
            if (i < 10) {
                world.setBlockState(pos, state.with(SLATE_BITES, i + 1), Block.NOTIFY_ALL);
            } else {
                world.removeBlock(pos, false);
                world.emitGameEvent(player, GameEvent.BLOCK_DESTROY, pos);
            }

            return ActionResult.SUCCESS;
        }
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        return null;
    }

    @Override
    public boolean canEnterFromDirection(Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        return false;
    }

    @Override
    public EnumSet<Direction> possibleExitDirections(BlockPos pos, BlockState bs, World world) {
        return null;
    }

    @Override
    public BlockState startEnergized(BlockPos pos, BlockState bs, World world) {
        var newState = bs.with(ENERGIZED, true);
        world.setBlockState(pos, newState);

        return newState;
    }

    @Override
    public boolean isEnergized(BlockPos pos, BlockState bs, World world) {
        return bs.get(ENERGIZED);
    }

    @Override
    public BlockState endEnergized(BlockPos pos, BlockState bs, World world) {
        var newState = bs.with(ENERGIZED, false);
        world.setBlockState(pos, newState);
        return newState;
    }
}
