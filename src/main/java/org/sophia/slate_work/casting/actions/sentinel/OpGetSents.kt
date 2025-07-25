package org.sophia.slate_work.casting.actions.sentinel

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import org.sophia.slate_work.misc.CircleHelper

object OpGetSents : ConstMediaAction {
    override val argc: Int
        get() = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        if (env !is CircleCastEnv) {
            throw MishapNoSpellCircle()
        }
        val loci = CircleHelper.getSentLoci(env)
        val list: List<Iota> = loci.map { Vec3Iota(it.sentPos) }

        return list.asActionResult
    }
}