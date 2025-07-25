package org.sophia.slate_work.casting.actions.sentinel

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.util.math.Vec3d
import org.sophia.slate_work.casting.mishap.MishapListLength
import org.sophia.slate_work.casting.mishap.MishapNoSentinelLoci
import org.sophia.slate_work.misc.CircleHelper

object OpSetSents : ConstMediaAction {
    override val argc: Int
        get() = 1
    override val mediaCost: Long
        get() = MediaConstants.DUST_UNIT

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        if (env !is CircleCastEnv){
            throw MishapNoSpellCircle()
        }
        val inputList = args.getList(0,argc)
        val realList = mutableListOf<Vec3d>()
        var i = 0
        for (z in inputList){
            val vec = inputList.toList().getVec3(i)
            env.assertVecInRange(vec)
            realList.add(vec)
            i++
        }
        val loci = CircleHelper.getSentLoci(env)
        if (loci.isEmpty()) {
            throw MishapNoSentinelLoci()
        }

        if (realList.size > loci.size){ // If there are too many items in the list, mishap
            throw MishapListLength(loci.size, realList.size)
        }
        i = 0
        for (z in realList){
            loci[i].sentPos = realList[i]
            i++
        }

        return listOf()
    }
}