package org.sophia.slate_work.casting.actions

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import miyucomics.hexpose.iotas.IdentifierIota
import miyucomics.hexpose.iotas.ItemStackIota
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.util.math.Vec3d
import org.sophia.slate_work.blocks.StorageLociEntity
import org.sophia.slate_work.casting.mishap.MishapNoJars
import org.sophia.slate_work.misc.CircleHelper
import org.sophia.slate_work.misc.CircleHelper.getItemVariant

object OpGetItem : SpellAction {
    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()
        val storages = CircleHelper.getStorage(env)
        if (storages.isEmpty())
            throw MishapNoJars()
        // TODO: Make this really work, for real
        val z = args[0]

        var target = args.getItemVariant(0,argc)
        val pos = args.getVec3(2,argc)
        val amount = args.getInt(1,argc)
        env.assertVecInRange(pos)

        return SpellAction.Result(
            Spell(storages, pos, target, amount),
            1L,
            listOf()
        )
    }

    private data class Spell(val storages: List<StorageLociEntity>, val vec: Vec3d, val target: ItemVariant, val amount: Int) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val list = CircleHelper.getLists(env as CircleCastEnv)
            val z = list.get(target)
            if (z == null || z.storageLociEntity.getSlot(target) == null)
                return
            val summon = z.storageLociEntity.removeStack(z.storageLociEntity.getSlot(target)!!,amount)
            env.world.spawnEntity(ItemEntity(env.world,vec.x,vec.y,vec.z,
                ItemStack(summon.left.item, summon.right.toInt())))
        }
    }
}