package org.sophia.slate_work.casting.actions

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import miyucomics.hexpose.iotas.ItemStackIota
import net.minecraft.nbt.NbtCompound
import org.sophia.slate_work.casting.SearchingBasedEnv
import org.sophia.slate_work.casting.mishap.MishapNoStorageLoci
import org.sophia.slate_work.misc.CircleHelper

@Suppress("UnstableApiUsage")
object OpContainsItem : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()
        val storages = CircleHelper.getStorage(env)
        if (storages.isEmpty())
            throw MishapNoStorageLoci(null)

        val hexTemp = args.getList(0, argc)

        val hex = ArrayList<Iota>()
        for (z in hexTemp){
            hex.add(z)
        }

        val list = CircleHelper.getLists(storages)
        var data: NbtCompound
        val tempStack = env.circleState().currentImage.stack.toMutableList()
        tempStack.removeLastOrNull() //Clear the args for the Spell
        var found = false

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

            if (resolution.resolutionType == ResolvedPatternType.ERRORED){ // If the inner Hex errors, stop running
                break
            }

            val realStack = vm.image.stack.reversed()
            found = realStack.getBool(0, 0)
            if (found) break
        }
        return found.asActionResult
    }
}