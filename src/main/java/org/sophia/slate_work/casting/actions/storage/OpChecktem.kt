package org.sophia.slate_work.casting.actions.storage

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.BooleanIota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import org.sophia.slate_work.casting.contuinations.FrameCheckItems
import org.sophia.slate_work.casting.contuinations.JankyMaybe
import org.sophia.slate_work.misc.CircleHelper

@Suppress("DATA_CLASS_INVISIBLE_COPY_USAGE_WARNING")
object OpCheckItem : Action {

    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation,
    ): OperationResult {
        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()

        val stack = image.stack.toMutableList()
        val hex = stack.getList(stack.lastIndex, stack.size)
        stack.removeLastOrNull()
        val storages = CircleHelper.getStorage(env)
        val toCheck = CircleHelper.getOnlySlots(storages)


        if (toCheck.isEmpty()){
            val stack = image.stack.toMutableList()
            stack.add(BooleanIota(false))
            return OperationResult(image.withUsedOp().copy(stack = stack), listOf(),
                continuation, HexEvalSounds.SPELL)
        }

        val image2 = image.withUsedOp().copy(stack = stack)
        val frame = FrameCheckItems(hex,stack,toCheck.toMutableList(), JankyMaybe.FIRST)

        return OperationResult(image2,
            listOf(),
            continuation.pushFrame(frame), HexEvalSounds.SPELL)
    }
}