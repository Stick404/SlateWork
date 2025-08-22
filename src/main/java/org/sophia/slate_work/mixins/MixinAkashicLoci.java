package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicRecord;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.sophia.slate_work.blocks.AkashicRecordLoci;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;

@Mixin(BlockAkashicRecord.class)
public abstract class MixinAkashicLoci extends Block implements ICircleComponent {
    @Unique
    private static final BooleanProperty ENERGIZED = BooleanProperty.of("energized");
    public MixinAkashicLoci(Settings settings) {
        super(settings);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void slate_work$blockData(Settings p_49795_, CallbackInfo ci){
        this.setDefaultState(this.stateManager.getDefaultState().with(ENERGIZED, false));
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        return AkashicRecordLoci.acceptControlFlow(imageIn, env, enterDir, pos, bs, world, (BlockAkashicRecord)(Object)this);
    }

    @Override
    public boolean canEnterFromDirection(Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        return true;
    }

    @Override
    public EnumSet<Direction> possibleExitDirections(BlockPos pos, BlockState bs, World world) {
        return EnumSet.allOf(Direction.class);
    }

    @Override
    public Pair<BlockPos, Direction> exitPositionFromDirection(BlockPos pos, Direction dir) {
        return ICircleComponent.super.exitPositionFromDirection(pos, dir);
    }

    @Override
    public BlockState startEnergized(BlockPos pos, BlockState bs, World world) {
        var newState = bs.with(ENERGIZED, true);
        world.setBlockState(pos, newState);

        return newState;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ENERGIZED);
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
