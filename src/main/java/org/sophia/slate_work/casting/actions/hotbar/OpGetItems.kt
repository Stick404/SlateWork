package org.sophia.slate_work.casting.actions.hotbar

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtHelper
import org.sophia.slate_work.blocks.entities.HotbarLociEntity
import ram.talia.moreiotas.api.casting.iota.ItemStackIota

object OpGetItems : ConstMediaAction {
    override val argc: Int
        get() = 0

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): List<Iota> {
        if (env !is CircleCastEnv) {
            throw MishapNoSpellCircle()
        }
        val vec = NbtHelper.toBlockPos(env.circleState().currentImage.userData, "hotbar_loci").get()
        val entity = env.world.getBlockEntity(vec)
        val list = ArrayList<Iota>()
        if (entity is HotbarLociEntity){
            for (z in entity.stacks){
                list.add(ItemStackIota.createFiltered(z as ItemStack))
            }
        }
        return listOf(ListIota(list))
    }
}