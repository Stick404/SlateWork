package org.sophia.slate_work.casting.actions

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.getItemEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.entity.ItemEntity
import org.sophia.slate_work.blocks.entities.StorageLociEntity
import org.sophia.slate_work.casting.mishap.MishapNoJars
import org.sophia.slate_work.misc.CircleHelper

object OpStoreItem : SpellAction {
    override val argc: Int = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()
        val storages = CircleHelper.getStorage(env)
        if (storages.isEmpty())
            throw MishapNoJars()
        val entity = args.getItemEntity(0, argc)
        env.assertEntityInRange(entity)

        return SpellAction.Result(
            Spell(entity,storages),
            entity.stack.count.toLong(), //TODO: Make this LOG, rather than LIN
            listOf(ParticleSpray.burst(entity.pos,1.0))
        )
    }

    private data class Spell(val entity: ItemEntity, val storages: List<StorageLociEntity>) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            // TODO: Make this harder?
            if (!entity.isAlive)
                return

            // In case we don't find it, we don't want to recalc *again*
            val itemE = entity.stack
            val list = CircleHelper.getStorage(env as CircleCastEnv)
            val hashMap = CircleHelper.getLists(list)
            if (hashMap.contains(ItemVariant.of(itemE.item,itemE.nbt))) {
                val slot = hashMap.get(ItemVariant.of(itemE.item,itemE.nbt))!!
                val targ = slot.storageLociEntity.getSlot(slot.item)!! // *shouldn't* be null
                val item = slot.storageLociEntity.getStack(targ)
                item.right += itemE.count
                entity.kill()
                return
            }
            // If its not a known item yet...
            for (z in list){
                val x = z.isFull
                if (x != -1) {
                    z.setStack(x, ItemVariant.of(itemE.item,itemE.nbt),itemE.count.toLong())
                    entity.kill()
                    return
                }
            }
        }
    }
}