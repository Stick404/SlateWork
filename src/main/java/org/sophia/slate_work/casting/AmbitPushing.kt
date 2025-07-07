package org.sophia.slate_work.casting

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import net.minecraft.nbt.NbtHelper
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import kotlin.random.Random

class AmbitPushing(private val env: CastingEnvironment) : CastingEnvironmentComponent.IsVecInRange{
    private val key = Key(Keygen.randid())
    override fun getKey(): CastingEnvironmentComponent.Key<*>? = key

    override fun onIsVecInRange(vec: Vec3d?, inAmbit: Boolean): Boolean {
        if (inAmbit || env !is CircleCastEnv)
            return inAmbit

        val state = env.circleState()
        val data = state.currentImage.userData
        val hasPushedPos = NbtHelper.toBlockPos(data.getCompound("ambit_pushed_pos"))
        val hasPushedNeg = NbtHelper.toBlockPos(data.getCompound("ambit_pushed_neg"))

        val envBounds = state.bounds
        val bound = Box(
            envBounds.minX + hasPushedNeg.x,
            envBounds.minY + hasPushedNeg.y,
            envBounds.minZ + hasPushedNeg.z,
            envBounds.maxX + hasPushedPos.x,
            envBounds.maxY + hasPushedPos.y,
            envBounds.maxZ + hasPushedPos.z,
            )

        return bound.contains(vec)
    }

    private object Keygen { //code from HexSky
        val rand = Random(6485284256)
        fun randid() = rand.nextInt()
    }

    class Key(val id: Int) : CastingEnvironmentComponent.Key<AmbitPushing>
}