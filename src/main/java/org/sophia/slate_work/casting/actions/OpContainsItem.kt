package org.sophia.slate_work.casting.actions

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.BooleanIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import miyucomics.hexpose.iotas.ItemStackIota
import net.minecraft.nbt.NbtCompound
import org.sophia.slate_work.casting.SearchingBasedEnv
import org.sophia.slate_work.misc.CircleHelper

object OpContainsItem : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack.toMutableList()
        val iota = stack.reversed().getList(0)
        stack.removeLastOrNull()

        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()

        var boolean = false
        var data = NbtCompound()

        for (z in CircleHelper.getLists(env)){
            val ctx = SearchingBasedEnv(env)
            val vm = CastingVM.empty(ctx)
            val realHex = ArrayList<Iota>()
            for (x in iota){
                realHex.add(x)
            }
            val newStack = ArrayList<Iota>()
            newStack.addAll(stack)
            newStack.add(ItemStackIota(z.key.toStack()))
            vm.image = vm.image.copy(newStack, userData = data)

            vm.queueExecuteAndWrapIotas(realHex, env.world)
            boolean = vm.image.stack.reversed().getBool(0,1)
            data = vm.image.userData.copy()
            // about ~4 hours of fighting. Just to learn 2 things
            // 1: `#queueExecuteAndWrapIota` and `#queueExecuteAndWrapIota**s**` are different methods
            // 2: the stack is *reversed* when reading it from the image directly (IE: end of the list is the top of the stack)
            // its fucking 2:40 AM as of writing
            if (boolean) break
        }
        stack.add(BooleanIota(boolean))
        val newImage = image.copy(stack)
        return OperationResult(newImage,listOf(),continuation, HexEvalSounds.NORMAL_EXECUTE)
    }
}