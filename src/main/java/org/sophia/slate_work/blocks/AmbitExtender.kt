package org.sophia.slate_work.blocks

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus
import at.petrak.hexcasting.api.casting.circles.ICircleComponent.ControlFlow
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.BooleanIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapBoolDirectrixEmptyStack
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapBoolDirectrixNotBool
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.utils.putCompound
import com.mojang.datafixers.util.Pair
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3i
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt


class AmbitExtender(settings: Settings) : AbstractSlate(settings) {
    override fun acceptControlFlow(
        imageIn: CastingImage?, env: CircleCastEnv?, enterDir: Direction?, pos: BlockPos?,
        bs: BlockState?, world: ServerWorld?,
    ): ControlFlow? {
        val exitDirsSet = this.possibleExitDirections(pos, bs, world)
        exitDirsSet.remove(enterDir!!.opposite)
        val exitDirs = exitDirsSet.stream()
            .map<Pair<BlockPos?, Direction?>?> { dir: Direction? -> this.exitPositionFromDirection(pos, dir) }
        val data = imageIn!!.userData.copy()

        val stack: ArrayList<Iota> = ArrayList(imageIn.stack)
        if (stack.isEmpty()) {
            this.fakeThrowMishap(
                pos, bs, imageIn, env,
                MishapNotEnoughArgs(1,0)
            )
            return ControlFlow.Stop()
        }

        val last: Iota = stack.removeAt(stack.size - 1)
        if (last !is Vec3Iota) {
            this.fakeThrowMishap(
                pos, bs, imageIn, env,
                MishapInvalidIota.ofType(last, 0,"vector")
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

        val posPushed = sqrt(willPushPos.getSquaredDistance(BlockPos(0,0,0)).absoluteValue)
        val negPushed = sqrt(willPushNeg.getSquaredDistance(BlockPos(0, 0, 0)).absoluteValue)
        val cost = ((posPushed + negPushed).pow(2).toLong() * MediaConstants.SHARD_UNIT)
        val extracted = env?.extractMedia(cost,false)
        if (0L != extracted) {
            this.fakeThrowMishap(
                pos, bs, imageIn, env,
                MishapNotEnoughMedia(cost)
            )
            return ControlFlow.Stop()
        }
        data.putCompound("ambit_pushed_pos", NbtHelper.fromBlockPos(willPushPos))
        data.putCompound("ambit_pushed_neg", NbtHelper.fromBlockPos(willPushNeg))

        return ControlFlow.Continue(
            imageIn.copy(stack, userData = data), exitDirs.toList()
        )
    }

    override fun fakeThrowMishap(
        pos: BlockPos?,
        bs: BlockState?,
        image: CastingImage?,
        env: CircleCastEnv?,
        mishap: Mishap?
    ) {
        super.fakeThrowMishap(pos, bs, image, env, mishap)
    }
}