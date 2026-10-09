package org.sophia.slate_work.casting.actions.storage

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import gay.`object`.ioticblocks.utils.mishapBadEntityOrBlock
import net.minecraft.text.Text
import org.sophia.slate_work.casting.contuinations.FrameGetItems
import org.sophia.slate_work.casting.contuinations.JankyMaybe
import org.sophia.slate_work.misc.CircleHelper
import org.sophia.slate_work.registries.SlateWorksPatternRegistry

@Suppress("DATA_CLASS_INVISIBLE_COPY_USAGE_WARNING")
object OpGetItem : Action {

    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation,
    ): OperationResult {
        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()
        println("AAA")

        var stack = image.stack
        if (stack.length() < 1) {
            return OperationResult(image,
                listOf(OperatorSideEffect.DoMishap(MishapNotEnoughArgs(1, 0),
                    Mishap.Context(SlateWorksPatternRegistry.GET_ITEM, Text.translatable("hexcasting.action.slate_work:get_item")))),
                continuation, HexEvalSounds.SPELL.get())
        }
        println("Past length")

        val iota = stack.last()
        if (iota !is ListIota) {
            return OperationResult(image,
                listOf(OperatorSideEffect.DoMishap(MishapInvalidIota.of(iota, 0, "class.list"),
                    Mishap.Context(SlateWorksPatternRegistry.GET_ITEM, Text.translatable("hexcasting.action.slate_work:get_item")))),
                continuation, HexEvalSounds.SPELL.get())
        }
        println("Right Iota")

        val code = iota.list
        stack = stack.init()
        val storages = CircleHelper.getStorage(env)
        val toCheck = CircleHelper.getOnlySlots(storages)

        val frame = FrameGetItems(code,stack, toCheck.toMutableList(), null, JankyMaybe.FIRST)
        val image2 = image.withUsedOp().copy(stack = stack)

        val media = env.extractMedia(((storages.size.toDouble()*0.25)* MediaConstants.DUST_UNIT.toDouble()).toLong(), false)
        if (media != 0L) {
            throw MishapNotEnoughMedia(media)
        }

        return OperationResult(image2,
            listOf(),
            continuation.pushFrame(frame), HexEvalSounds.SPELL.get())
    }
}