package org.sophia.slate_work.casting.contuinations

import at.petrak.hexcasting.api.casting.SpellList
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
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.putList
import at.petrak.hexcasting.api.utils.serializeToNBT
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import miyucomics.hexpose.iotas.ItemStackIota
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import org.sophia.slate_work.misc.CircleHelper

// Almost exactly like FrameGetItems, but it returns early/with only bool!
@Suppress("UnstableApiUsage", "DATA_CLASS_INVISIBLE_COPY_USAGE_WARNING")
class FrameCheckItems(
    val code: SpellList,
    val baseStack: MutableList<Iota>,
    val toCheck: MutableList<CircleHelper.ItemSlot>,
    var isFirst: JankyMaybe = JankyMaybe.RUNNING
) : ContinuationFrame {

    override val type: ContinuationFrame.Type<*>
        get() = TYPE

    override fun breakDownwards(stack: List<Iota>): Pair<Boolean, List<Iota>> {
        return true to listOf()
    }

    // Kind of copies what Thoth's (FrameForEach) does
    override fun evaluate(continuation: SpellContinuation, level: ServerWorld, harness: CastingVM): CastResult {
        val stack = baseStack.toMutableList()
        val slot = if (isFirst != JankyMaybe.LAST && !toCheck.isEmpty()){
            toCheck.removeFirst()
        } else {
            isFirst = JankyMaybe.LAST
            null
        }

        var hasFound = false
        val realStack = harness.image.stack.reversed().toMutableList()
        val sideEffect: MutableList<OperatorSideEffect> = mutableListOf()

        if (isFirst != JankyMaybe.FIRST && isFirst != JankyMaybe.LAST){
            try {
                if (harness.env !is CircleCastEnv) {
                    throw MishapNoSpellCircle() // Chloe I know you are reading this. No.
                }

                if (realStack.getBool(0, 0)){
                    hasFound = true
                }
                realStack.removeLast()
            } catch (e : Mishap){
                sideEffect.add(OperatorSideEffect.DoMishap(e, Mishap.Context(null,
                    Text.translatable("hexcasting.action.slate_work:check_item"))))
                return CastResult(
                    ListIota(code),
                    continuation,
                    harness.image.withUsedOp().copy(stack = stack),
                    sideEffect,
                    ResolvedPatternType.ERRORED,
                    HexEvalSounds.NORMAL_EXECUTE
                )
            }
        }

        val cont = if (isFirst != JankyMaybe.LAST) {
            stack.add(ItemStackIota(slot!!.item.toStack(if (slot.count > Int.MAX_VALUE) Int.MAX_VALUE else slot.count.toInt())))
            when (isFirst){
                JankyMaybe.PENULTIMATE -> {
                    continuation
                        .pushFrame(FrameCheckItems(code,baseStack,toCheck, JankyMaybe.LAST))
                        .pushFrame(FrameEvaluate(code,true))
                }
                else -> { // When FIRST or RUNNING push the frame
                    continuation
                        .pushFrame(FrameCheckItems(code,baseStack,toCheck))
                        .pushFrame(FrameEvaluate(code,true))
                }
            }
        } else {
            stack.add(BooleanIota(hasFound))
            continuation
        }

        return CastResult(
            ListIota(code),
            cont,
            harness.image.withUsedOp().copy(stack = stack),
            sideEffect,
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.NORMAL_EXECUTE
        )
    }

    override fun serializeToNBT(): NbtCompound {
        val compound = NbtCompound()
        compound.putList("stack", baseStack.serializeToNBT() as NbtList)
        compound.putList("code", code.serializeToNBT() as NbtList)

        val listCheck = NbtList()
        for (z in toCheck){
            val tempCompound = NbtCompound()
            tempCompound.put("item",z.item.toNbt())
            // Location is to help with keeping track of valid items; so there cant be dupe bugs
            tempCompound.put("location", NbtHelper.fromBlockPos(z.storageLociEntity.pos))
            listCheck.add(tempCompound)
        }
        compound.putList("to_check",listCheck)
        compound.putString("jank_maybe", this.isFirst.name)
        return compound
    }

    override fun size(): Int = baseStack.size

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameCheckItems> = object : ContinuationFrame.Type<FrameCheckItems> {
            override fun deserializeFromNBT(tag: NbtCompound, world: ServerWorld): FrameCheckItems? {
                val code = HexIotaTypes.LIST.deserialize(tag.getList("code", NbtElement.COMPOUND_TYPE), world)!!.list
                val stack = HexIotaTypes.LIST.deserialize(tag.getList("stack", NbtElement.COMPOUND_TYPE), world)!!.list.toList()
                val toCheck = listOf<CircleHelper.ItemSlot>().toMutableList()
                for (z in tag.getList("to_check", NbtElement.COMPOUND_TYPE)){
                    val slot = CircleHelper.ItemSlot.load(z as NbtCompound,world)
                    if (slot != null){
                        toCheck.add(slot)
                    }
                }
                val stepEval = JankyMaybe.valueOf(tag.getString("jank_maybe"))
                return FrameCheckItems(code, stack as MutableList<Iota>,toCheck, stepEval)
            }

        }
    }
}