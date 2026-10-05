package org.sophia.slate_work.casting.contuinations

import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.iota.BooleanIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import com.mojang.serialization.MapCodec
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import org.sophia.slate_work.misc.CircleHelper
import ram.talia.moreiotas.api.casting.iota.ItemStackIota

// Almost exactly like FrameGetItems, but it returns early/with only bool!
@Suppress("DATA_CLASS_INVISIBLE_COPY_USAGE_WARNING")
class FrameCheckItems(
    val code: TreeList<Iota>,
    val baseStack: TreeList<Iota>,
    val toCheck: MutableList<CircleHelper.ItemSlot>,
    var isFirst: JankyMaybe = JankyMaybe.FIRST
) : ContinuationFrame {

    override val type: ContinuationFrame.Type<*>
        get() = TYPE

    override fun breakDownwards(stack: TreeList<Iota>): Pair<Boolean, TreeList<Iota>> {
        return true to stack
    }

    // Kind of copies what Thoth's (FrameForEach) does
    override fun evaluate(continuation: SpellContinuation, level: ServerWorld, harness: CastingVM): CastResult {
        val stack = baseStack
        val slot = if (isFirst != JankyMaybe.LAST && toCheck.isNotEmpty()) {
            toCheck.removeFirst()
        } else {
            isFirst = JankyMaybe.LAST
            null
        }

        var hasFound = false
        val realStack = harness.image.stack
        val sideEffect: MutableList<OperatorSideEffect> = mutableListOf()

        if (isFirst != JankyMaybe.FIRST) {
            try {
                if (harness.env !is CircleCastEnv) {
                    throw MishapNoSpellCircle() // Chloe I know you are reading this. No.
                }

                if (realStack.getBool(0, 0)) {
                    hasFound = true
                }
                //realStack.removeLast()
            } catch (e: Mishap) {
                sideEffect.add(
                    OperatorSideEffect.DoMishap(
                        e, Mishap.Context(
                            null,
                            Text.translatable("hexcasting.action.slate_work:check_item")
                        )
                    )
                )
                return CastResult(
                    ListIota(code),
                    continuation,
                    harness.image.withUsedOp().copy(stack = stack),
                    sideEffect,
                    ResolvedPatternType.ERRORED,
                    HexEvalSounds.NORMAL_EXECUTE.get()
                )
            }
        }

        val cont = if (hasFound){
            stack.add(BooleanIota(true))
            return CastResult(
                ListIota(code),
                continuation,
                harness.image.withUsedOp().copy(stack = stack),
                sideEffect,
                ResolvedPatternType.EVALUATED,
                HexEvalSounds.NORMAL_EXECUTE.get()
            )
        } else if (isFirst != JankyMaybe.LAST) {
            stack.add(ItemStackIota.createFiltered(slot!!.item.toStack(if (slot.count > Int.MAX_VALUE) Int.MAX_VALUE else slot.count.toInt())))
            when (isFirst){
                JankyMaybe.PENULTIMATE -> {
                    continuation
                        .pushFrame(FrameCheckItems(code, baseStack, toCheck, JankyMaybe.LAST))
                        .pushFrame(FrameEvaluate(code,true))
                }
                else -> { // When FIRST or RUNNING push the frame
                    continuation
                        .pushFrame(FrameCheckItems(code, baseStack, toCheck, JankyMaybe.RUNNING))
                        .pushFrame(FrameEvaluate(code,true))
                }
            }
        } else {
            stack.add(BooleanIota(false))
            continuation
        }

        return CastResult(
            ListIota(code),
            cont,
            harness.image.withUsedOp().copy(stack = stack),
            sideEffect,
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.NORMAL_EXECUTE.get()
        )
    }

    override fun size(): Int = baseStack.size

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameCheckItems> = object : ContinuationFrame.Type<FrameCheckItems> {
            override fun codec(): MapCodec<FrameCheckItems> {
                TODO("Not yet implemented")
            }

            override fun streamCodec(): PacketCodec<RegistryByteBuf, FrameCheckItems> {
                TODO("Not yet implemented")
            }
        }
    }
}