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
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import miyucomics.hexpose.iotas.ItemStackIota
import org.apache.commons.codec.binary.Hex
import org.sophia.slate_work.casting.SearchingBasedEnv
import org.sophia.slate_work.misc.CircleHelper

object OpContainsItem : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack.toMutableList()
        val iota = stack.removeLastOrNull() ?: throw MishapNotEnoughArgs(1, 0)

        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()

        var boolean: Boolean = false

        for (z in CircleHelper.getLists(env)){
            val ctx = SearchingBasedEnv(env)
            val vm = CastingVM.empty(ctx)
            val innerStack = vm.image.stack.toMutableList()
            innerStack.addAll(stack)
            innerStack.add(ItemStackIota(z.key.toStack(1)))
            val copy = vm.image.copy(innerStack)
            vm.image = copy
            val temp = listOf<Iota>(
                PatternIota(HexPattern.fromAngles("aqae",HexDir.SOUTH_EAST))
            )

            var z = vm.queueExecuteAndWrapIota(ListIota(temp),ctx.world)
            println(z)
            //boolean = vm.image.stack.getBool(0)
            if (boolean) break
        }
        stack.add(BooleanIota(boolean))
        val newImage = image.copy(stack = stack)
        return OperationResult(newImage,listOf(),continuation, HexEvalSounds.NORMAL_EXECUTE)
    }
}