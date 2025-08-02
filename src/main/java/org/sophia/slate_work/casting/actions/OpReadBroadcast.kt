package org.sophia.slate_work.casting.actions

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.mishaps.MishapStackSize
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import org.sophia.slate_work.misc.KnownBroadcasters

object OpReadBroadcast : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val newStack = ArrayList(image.stack)
        if (newStack.isEmpty) {
            throw MishapStackSize()
        }
        val pos = newStack.getBlockPos(0,0)
        newStack.removeLast()
        newStack.add(KnownBroadcasters.getOrLoad(env.world, pos))

        val newImage = image.copy(stack = newStack)
        return OperationResult(newImage, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE)
    }
}