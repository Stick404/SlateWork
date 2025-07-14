package org.sophia.slate_work.blocks

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent
import at.petrak.hexcasting.api.casting.circles.ICircleComponent.ControlFlow
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.utils.putCompound
import com.mojang.datafixers.util.Pair
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import org.sophia.slate_work.casting.mishap.MishapSpellCircleInvalidIota
import org.sophia.slate_work.casting.mishap.MishapSpellCircleMedia
import org.sophia.slate_work.casting.mishap.MishapSpellCircleNotEnoughArgs
import java.util.*
import java.util.stream.Stream
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt


class AmbitExtender: BlockCircleComponent {

    constructor(settings: Settings) : super(settings) {
        this.setDefaultState(this.stateManager.getDefaultState().with(ENERGIZED, false))
    }


    override fun acceptControlFlow(
        imageIn: CastingImage?, env: CircleCastEnv?, enterDir: Direction?, pos: BlockPos?,
        bs: BlockState?, world: ServerWorld?,
    ): ControlFlow? {
        val data = imageIn!!.userData.copy()

        val exitDirsSet = this.possibleExitDirections(pos, bs, world)
        exitDirsSet.remove(enterDir!!.opposite)
        val exitDirs: Stream<Pair<BlockPos?, Direction?>?>? = exitDirsSet.stream()
            .map<Pair<BlockPos?, Direction?>?> { dir: Direction? -> this.exitPositionFromDirection(pos, dir) }

        val stack: ArrayList<Iota> = ArrayList(imageIn.stack)
        if (stack.isEmpty()) { // Feels silly, but this is what Hex does
            this.fakeThrowMishap(
                pos, bs, imageIn, env,
                MishapSpellCircleNotEnoughArgs(1,0,pos!!)
            )
            return ControlFlow.Stop()
        }

        val last: Iota = stack.removeAt(stack.size - 1)
        if (last !is Vec3Iota) {
            this.fakeThrowMishap(
                pos, bs, imageIn, env,
                MishapSpellCircleInvalidIota.ofType(last, 0,"vector",pos!!)
            )
            return ControlFlow.Stop()
        }

        val toPush = BlockPos(last.vec3.x.toInt(),last.vec3.y.toInt(),last.vec3.z.toInt())

        val hasPushedPos = NbtHelper.toBlockPos(data.getCompound("ambit_pushed_pos"))
        val hasPushedNeg = NbtHelper.toBlockPos(data.getCompound("ambit_pushed_neg"))

        var willPushPos = hasPushedPos
        var willPushNeg = hasPushedNeg

        if (toPush.x >= 0) {
            willPushPos = willPushPos.add(toPush.x,0,0)
        } else {
            willPushNeg = willPushNeg.add(toPush.x,0,0)
        }
        if (toPush.y >= 0) {
            willPushPos = willPushPos.add(0,toPush.y,0)
        } else {
            willPushNeg = willPushNeg.add(0,toPush.y,0)
        }
        if (toPush.y >= 0) {
            willPushPos = willPushPos.add(0,0,toPush.z)
        } else {
            willPushNeg = willPushNeg.add(0,0,toPush.z)
        }

        val posPush = willPushPos.getManhattanDistance(BlockPos(0,0,0)).absoluteValue
        val negPush = willPushNeg.getManhattanDistance(BlockPos(0, 0, 0)).absoluteValue

        val posPushed = hasPushedPos.getManhattanDistance(BlockPos(0,0,0)).absoluteValue
        val negPushed = hasPushedNeg.getManhattanDistance(BlockPos(0,0,0)).absoluteValue

        val cost = ((((posPush + negPush).toDouble().pow(2)) -(posPushed + negPushed).toDouble().pow(2)).toLong() * MediaConstants.SHARD_UNIT)
        val extracted = env?.extractMedia(cost,false)
        if (0L != extracted) {
            this.fakeThrowMishap(
                pos, bs, imageIn, env,
                MishapSpellCircleMedia(cost,pos!!)
            )
            return ControlFlow.Stop()
        }
        data.putCompound("ambit_pushed_pos", NbtHelper.fromBlockPos(willPushPos))
        data.putCompound("ambit_pushed_neg", NbtHelper.fromBlockPos(willPushNeg))

        return ControlFlow.Continue(
            imageIn.copy(stack, userData = data), exitDirs?.toList()
        )
    }

    // Ok fine, Kotlin can be nice time to time
    override fun canEnterFromDirection(p0: Direction?, p1: BlockPos?, p2: BlockState?, p3: ServerWorld?) = true
    override fun possibleExitDirections(p0: BlockPos?, p1: BlockState?, p2: World?) = EnumSet.allOf(Direction::class.java)!!
    override fun normalDir(p0: BlockPos?, p1: BlockState?, p2: World?, p3: Int) = Direction.UP
    override fun particleHeight(p0: BlockPos?, p1: BlockState?, p2: World) = 0.5f
}