package org.sophia.slate_work.casting

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.sophia.slate_work.blocks.entities.SentinelLociEntity
import org.sophia.slate_work.blocks.impetus.ListeningImpetusEntity
import kotlin.random.Random

class CircleAmbitChanges(private val env: CastingEnvironment) : CastingEnvironmentComponent.IsVecInRange{
    private val key = Key(Keygen.randid())
    private val radius = 4
    override fun getKey(): CastingEnvironmentComponent.Key<*> = key

    override fun onIsVecInRange(vec: Vec3d, inAmbit: Boolean): Boolean {
        if (inAmbit || env !is CircleCastEnv)
            return inAmbit

        /** This is for the Librarian Impetus **/
        if (env.impetus is ListeningImpetusEntity) {
            if (vec.squaredDistanceTo(env.impetus?.pos?.toCenterPos()) <= 16 * 16 +0.00000000001){
                return true
            }
        }

        val state = env.circleState()
        val data = state.currentImage.userData

        /** This is for the Ambit Extender **/
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
        if (bound.contains(vec)) {
            return true
        }

        /** This is for the Sentinel Cache **/
        val sents = data.getList("sentinel_loci", NbtElement.COMPOUND_TYPE.toInt())
        for (temp in sents){
            val posTemp = temp as NbtCompound
            val pos = NbtHelper.toBlockPos(posTemp.getCompound("pos"))
            val entity = env.world.getBlockEntity(pos)
            if (entity !is SentinelLociEntity){
                continue
            }
            // adding 0.00000000001 to avoid machine precision errors at specific angles. Source:
            // https://github.com/FallingColors/HexMod/blob/977ccba28b63a5df2b6e15fb29f82879a61f5134/Common/src/main/java/at/petrak/hexcasting/api/casting/eval/env/PlayerBasedCastEnv.java#L115C17-L115C93
            if (vec.squaredDistanceTo(entity.sentPos) <= radius * radius +0.00000000001){
                return true
            }
        }
        return false
    }

    private object Keygen { //code from HexSky
        val rand = Random(6485284256)
        fun randid() = rand.nextInt()
    }

    class Key(val id: Int) : CastingEnvironmentComponent.Key<CircleAmbitChanges>
}