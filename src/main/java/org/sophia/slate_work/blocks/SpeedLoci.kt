package org.sophia.slate_work.blocks

import at.petrak.hexcasting.api.casting.circles.ICircleComponent.ControlFlow
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import com.mojang.datafixers.util.Pair
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import org.sophia.slate_work.misc.CircleSpeedValue
import org.sophia.slate_work.mixins.MixinCircleExecInvoker
import java.util.stream.Stream
import kotlin.math.abs
import kotlin.math.roundToInt

class SpeedLoci : AbstractSlate {

    constructor(settings: Settings) : super(settings) {
        this.setDefaultState(this.stateManager.getDefaultState().with(ENERGIZED, false).with(FACING, Direction.NORTH).with(WATERLOGGED, false));
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>?) {
        super.appendProperties(builder)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType? {
        return BlockRenderType.MODEL;
    }

    override fun getOutlineShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape? {
        return when(state?.get(FACING)){
            Direction.DOWN -> createCuboidShape(0.0, 16 -4.0, 0.0, 16.0, 16.0, 16.0)
            Direction.UP -> createCuboidShape(0.0,0.0,0.0,16.0,4.0,16.0)
            Direction.NORTH -> createCuboidShape(0.0, 0.0, 16 -4.0, 16.0, 16.0, 16.0);
            Direction.SOUTH -> createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 4.0)
            Direction.WEST -> createCuboidShape(16 - 4.0, 0.0, 0.0, 16.0, 16.0, 16.0)
            Direction.EAST -> createCuboidShape(0.0, 0.0, 0.0, 4.0, 16.0, 16.0)
            else -> createCuboidShape(0.0,0.0,0.0,16.0,4.0,16.0)
        }
    }

    override fun getCullingShape(state: BlockState?, world: BlockView?, pos: BlockPos?): VoxelShape? {
        return when(state?.get(FACING)){
            Direction.DOWN -> createCuboidShape(0.0, 16 -4.0, 0.0, 16.0, 16.0, 16.0)
            Direction.UP -> createCuboidShape(0.0,0.0,0.0,16.0,4.0,16.0)
            Direction.NORTH -> createCuboidShape(0.0, 0.0, 16 -4.0, 16.0, 16.0, 16.0);
            Direction.SOUTH -> createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 4.0)
            Direction.WEST -> createCuboidShape(16 - 4.0, 0.0, 0.0, 16.0, 16.0, 16.0)
            Direction.EAST -> createCuboidShape(0.0, 0.0, 0.0, 4.0, 16.0, 16.0)
            else -> createCuboidShape(0.0,0.0,0.0,16.0,4.0,16.0)
        }
    }

    override fun acceptControlFlow(
        image: CastingImage?,
        env: CircleCastEnv?,
        direction: Direction,
        pos: BlockPos?,
        bs: BlockState?,
        world: ServerWorld?,
    ): ControlFlow? {
        val exitDirsSet = this.possibleExitDirections(pos, bs, world)
        exitDirsSet.remove(direction.opposite)
        val exitDirs: Stream<Pair<BlockPos?, Direction?>?>? = exitDirsSet.stream()
            .map<Pair<BlockPos?, Direction?>?> { dir: Direction? -> this.exitPositionFromDirection(pos, dir) }

        val stack = ArrayList(image!!.stack)
        val data = image.userData.copy()
        if (stack.isEmpty()) {
            this.fakeThrowMishap(
                pos, bs, image, env,
                MishapNotEnoughArgs(1,0)
            )
            return ControlFlow.Stop()
        }

        val last: Iota = stack.removeAt(stack.size -1)
        if (last !is DoubleIota) {
            this.fakeThrowMishap(
                pos, bs, image, env,
                MishapInvalidIota.ofType(last, 0,"vector")
            )
            return ControlFlow.Stop()
        }
        val double = last.double
        val rounded = double.roundToInt()
        // This chunk of code gets any positive Int
        // this does mean you could have a circle run a slate every 2147483647 ticks (or about 178 weeks)
        // sounds fine!
        if (!(abs(double - rounded) <= DoubleIota.TOLERANCE && rounded >= -1)) {
            this.fakeThrowMishap(
                pos, bs, image, env,
                MishapInvalidIota.of(last, 0,"int.positive")
            )
            return ControlFlow.Stop()
        }

        (env!!.circleState() as CircleSpeedValue).`slate_work$getRealValue`()
        val speed: Int = (env.circleState() as MixinCircleExecInvoker).`slate_work$getTickSpeed`()

        if (rounded >= speed || rounded == 0) { // the `rounded == 0` will make the circle run at its normal speed
            data.putInt("set_speed", rounded)
        }

        return ControlFlow.Continue(image.copy(userData = data, stack = stack), exitDirs?.toList())
    }
}