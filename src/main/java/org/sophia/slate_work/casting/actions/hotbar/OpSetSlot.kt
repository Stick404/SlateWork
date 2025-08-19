package org.sophia.slate_work.casting.actions.hotbar

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.getIntBetween
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import net.minecraft.nbt.NbtHelper
import org.sophia.slate_work.blocks.entities.HotbarLociEntity

object OpSetSlot: ConstMediaAction {
    override val argc: Int
        get() = 1
    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): List<Iota> {
        val slot = args.getIntBetween(0,0,5)
        if (env !is CircleCastEnv) {
            throw MishapNoSpellCircle()
        }
        val vec = NbtHelper.toBlockPos(env.circleState().currentImage.userData.getCompound("hotbar_loci"))
        val entity = env.world.getBlockEntity(vec)
        if (entity is HotbarLociEntity){
            entity.slot = slot;
        }

        return listOf()
    }
}