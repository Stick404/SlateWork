package org.sophia.slate_work.casting.actions.storage

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.getItemEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.api.misc.MediaConstants
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import org.sophia.slate_work.blocks.entities.StorageLociEntity
import org.sophia.slate_work.casting.mishap.MishapNoStorageLoci
import org.sophia.slate_work.misc.CircleHelper

object OpStoreItem : SpellAction {
    override val argc: Int = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()
        val storages = CircleHelper.getStorage(env)
        if (storages.isEmpty())
            throw MishapNoStorageLoci(null)
        val entity = args.getItemEntity(env.world, 0, argc)
        env.assertEntityInRange(entity)

        return SpellAction.Result(
            Spell(entity,storages),
            storages.size*(MediaConstants.DUST_UNIT/8),
            listOf(ParticleSpray.burst(entity.pos,1.0))
        )
    }

    private data class Spell(val itemEntity: ItemEntity, val storages: List<StorageLociEntity>) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (!itemEntity.isAlive)
                return

            // In case we don't find it, we don't want to recalc *again*
            val itemE = itemEntity.stack
            val list = CircleHelper.getStorage(env as CircleCastEnv)
            val hashMap = CircleHelper.getLists(list)
            if (hashMap.contains(ItemVariant.of(itemE.item, itemE.componentChanges))) {
                val slot = hashMap[ItemVariant.of(itemE.item,itemE.componentChanges)]!!
                val entity = env.world.getBlockEntity(slot.pos)
                if (entity !is StorageLociEntity) {
                    return
                }

                val targ = entity.getSlot(slot.item)!! // *shouldn't* be null
                val item = entity.getStack(targ)

                item.right += itemE.count
                entity.setStack(targ,item);
                itemEntity.stack = ItemStack.EMPTY
                return
            }
            // If its not a known item yet...
            for (z in list){
                val x = z.isFull
                if (x != -1) {
                    z.setStack(x, ItemVariant.of(itemE.item,itemE.componentChanges),itemE.count.toLong())
                    itemEntity.stack = ItemStack.EMPTY
                    return
                }
            }
        }
    }
}