package org.sophia.slate_work.casting.actions

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import org.sophia.slate_work.casting.contuinations.FrameGetItems
import org.sophia.slate_work.casting.contuinations.JankyMaybe
import org.sophia.slate_work.misc.CircleHelper

@Suppress("DATA_CLASS_INVISIBLE_COPY_USAGE_WARNING")
object OpGetItem : Action {

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

        // This `ItemVariant#blank` does not get used, and gets replaced
        val frame = FrameGetItems(hex,stack,toCheck.toMutableList(), null, JankyMaybe.FIRST)
        val image2 = image.withUsedOp().copy(stack = stack)

        val media = env.extractMedia(((storages.size.toDouble()*0.25)* MediaConstants.DUST_UNIT.toDouble()).toLong(), false)
        if (media != 0L) {
            throw MishapNotEnoughMedia(media)
        }


        return OperationResult(image2,
            listOf(),
            continuation.pushFrame(frame), HexEvalSounds.SPELL)
    }
}