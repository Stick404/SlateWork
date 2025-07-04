package org.sophia.slate_work.casting.actions

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import miyucomics.hexpose.iotas.ItemStackIota
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Vec3d
import org.sophia.slate_work.casting.SearchingBasedEnv
import org.sophia.slate_work.casting.mishap.MishapNoJars
import org.sophia.slate_work.misc.CircleHelper

object OpGetItem : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()
        val storages = CircleHelper.getStorage(env)
        if (storages.isEmpty())
            throw MishapNoJars()

        val hexTemp = args.getList(0,argc)

        val hex = ArrayList<Iota>()
        for (z in hexTemp){
            hex.add(z)
        }

        val list = CircleHelper.getLists(env)
        var data: NbtCompound
        val foundSlots = ArrayList<Triple<CircleHelper.ItemSlot, Vec3d, Int>>()

        for (z in list) {
            val ctx = SearchingBasedEnv(env)
            val vm = CastingVM.empty(ctx)
            val newStack = ArrayList<Iota>()
            val tempStack = env.circleState().currentImage.stack.toMutableList()
            tempStack.removeLastOrNull() //Clear the args for the Spell
            newStack.addAll(tempStack)

            // If it might be too long for an int, return IntMax
            newStack.add(ItemStackIota(z.key.toStack(if (z.value.count > Int.MAX_VALUE) Int.MAX_VALUE else z.value.count.toInt())))

            data = vm.image.userData.copy()
            vm.image = vm.image.copy(newStack, userData = data)

            vm.queueExecuteAndWrapIotas(hex, env.world)
            val realStack = vm.image.stack.reversed()
            if (realStack.getBool(0, 3)){
                val pos = realStack.getVec3(2,3)
                val amount = realStack.getInt(1,3)
                env.assertVecInRange(pos)
                foundSlots.add(Triple(z.value,pos,amount))
            }
        }


        return SpellAction.Result(
            Spell(foundSlots),
            1L,
            listOf()
        )
    }

    private data class Spell(var itemSlots: ArrayList<Triple<CircleHelper.ItemSlot, Vec3d, Int>>) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            for (itemSlotTup in itemSlots) {
                val itemSlot = itemSlotTup.first
                val vec = itemSlotTup.second
                val amount = itemSlotTup.third

                val summon = itemSlot.storageLociEntity.removeStack(
                    itemSlot.storageLociEntity.getSlot(itemSlot.item)!!, amount)
                env.world.spawnEntity(
                    ItemEntity(
                        env.world, vec.x, vec.y, vec.z,
                        ItemStack(summon.left.item, summon.right.toInt())
                    )
                )
            }
        }
    }
}