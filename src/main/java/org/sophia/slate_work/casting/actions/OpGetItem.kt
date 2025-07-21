package org.sophia.slate_work.casting.actions

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.api.misc.MediaConstants
import miyucomics.hexpose.iotas.ItemStackIota
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Vec3d
import org.sophia.slate_work.casting.SearchingBasedEnv
import org.sophia.slate_work.casting.mishap.MishapNoStorageLoci
import org.sophia.slate_work.misc.CircleHelper
import java.util.Optional

@Suppress("UnstableApiUsage")
object OpGetItem : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()
        val storages = CircleHelper.getStorage(env)
        if (storages.isEmpty())
            throw MishapNoStorageLoci(null)

        val hexTemp = args.getList(0,argc)

        val hex = ArrayList<Iota>()
        for (z in hexTemp){
            hex.add(z)
        }

        val list = CircleHelper.getLists(storages)
        var data: NbtCompound
        val foundSlots = ArrayList<Triple<CircleHelper.ItemSlot, Vec3d, Int>>()
        val tempStack = env.circleState().currentImage.stack.toMutableList()
        tempStack.removeLastOrNull() //Clear the args for the Spell

        // Frankly, this *should* use `FrameSearch`, but I can not be assed to do it
        for (z in list) {
            val ctx = SearchingBasedEnv(env)
            val vm = CastingVM.empty(ctx)
            val newStack = ArrayList<Iota>()
            newStack.addAll(tempStack)

            // If it might be too long for an int, return IntMax
            newStack.add(ItemStackIota(z.key.toStack(if (z.value.count > Int.MAX_VALUE) Int.MAX_VALUE else z.value.count.toInt())))

            data = vm.image.userData.copy()
            vm.image = vm.image.copy(newStack, userData = data)

            val resolution = vm.queueExecuteAndWrapIotas(hex, env.world)

            if (resolution.resolutionType == ResolvedPatternType.ERRORED){
                foundSlots.clear()
                break
            }


            val realStack = vm.image.stack.reversed()
            if (realStack.getBool(0, 3)){
                val pos = realStack.getVec3(1,3)
                val amount = realStack.getInt(2,3)
                env.assertVecInRange(pos)
                foundSlots.add(Triple(z.value,pos,amount))
            }
        }


        return SpellAction.Result(
            Spell(foundSlots),
            (((storages.size.toDouble()*0.25)* MediaConstants.DUST_UNIT.toDouble()).toLong()),
            listOf()
        )
    }

    private data class Spell(var itemSlots: ArrayList<Triple<CircleHelper.ItemSlot, Vec3d, Int>>) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            for (itemSlotTup in itemSlots) {
                val itemSlot = itemSlotTup.first
                val vec = itemSlotTup.second
                val amount = itemSlotTup.third

                val slot = itemSlot.storageLociEntity.getSlot(itemSlot.item)!!
                val summon = itemSlot.storageLociEntity.removeStack(slot, amount)
                val stack = ItemStack(summon.left.item, summon.right.toInt(), if (summon.left.nbt != null) Optional.of(
                    summon.left.nbt as NbtCompound
                ) else Optional.empty())

                while (stack.count > stack.maxCount){
                    val copy = stack.copy()
                    copy.count = stack.maxCount
                    env.world.spawnEntity(
                    ItemEntity(
                            env.world, vec.x, vec.y, vec.z, copy, 0.0, 0.0, 0.0)
                    )
                    stack.count -= stack.maxCount
                }
                env.world.spawnEntity(
                    ItemEntity(
                        env.world, vec.x, vec.y, vec.z, stack, 0.0,0.0, 0.0)
                )
            }
        }
    }
}