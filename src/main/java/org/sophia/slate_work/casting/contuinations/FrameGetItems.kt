package org.sophia.slate_work.casting.contuinations

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
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
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import org.sophia.slate_work.misc.CircleHelper
import java.util.*

@Suppress("UnstableApiUsage", "DATA_CLASS_INVISIBLE_COPY_USAGE_WARNING")
class FrameGetItems(
    val code: SpellList,
    val baseStack: List<Iota>,
    val toCheck: MutableList<CircleHelper.ItemSlot>,
    val isFirst: Boolean = false
) : ContinuationFrame {
    override val type: ContinuationFrame.Type<*>
        get() = TYPE

    override fun breakDownwards(stack: List<Iota>): Pair<Boolean, List<Iota>> {
        return true to listOf()
    }

    // Kind of copies what Thoth's (FrameForEach) does
    override fun evaluate(continuation: SpellContinuation, level: ServerWorld, harness: CastingVM): CastResult {
        val stack = baseStack.toMutableList()
        val slot = toCheck.first()
        if (!this.isFirst) {
            toCheck.removeFirst()
        }
        val realStack = harness.image.stack.reversed()
        val sideEffect: MutableList<OperatorSideEffect> = mutableListOf()

        if (!isFirst && !slot.item.isBlank){
            try {
                if (harness.env !is CircleCastEnv) {
                    throw MishapNoSpellCircle() // Chloe I know you are reading this. No.
                }

                if (realStack.getBool(0, 3)){
                    val amount = realStack.getInt(1,3)
                    val pos = realStack.getVec3(2,3)
                    harness.env.assertVecInRange(pos)
                    sideEffect.add(OperatorSideEffect.AttemptSpell(DumpDumbHexIsStupid(
                        Triple(slot,pos,amount)
                    )))
                }
            } catch (e : Mishap){
                sideEffect.add(OperatorSideEffect.DoMishap(e, Mishap.Context(null,
                    Text.translatable("hexcasting.action.slate_work:get_item"))))
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

        val cont = if (toCheck.isNotEmpty()) {
            stack.add(ItemStackIota(slot.item.toStack(if (slot.count > Int.MAX_VALUE) Int.MAX_VALUE else slot.count.toInt())))
            continuation
                .pushFrame(FrameGetItems(code,baseStack,toCheck))
                .pushFrame(FrameEvaluate(code,true))
        } else {
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
        return compound
    }

    override fun size(): Int = baseStack.size

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameGetItems> = object : ContinuationFrame.Type<FrameGetItems> {
            override fun deserializeFromNBT(tag: NbtCompound, world: ServerWorld): FrameGetItems? {
                val code = HexIotaTypes.LIST.deserialize(tag.getList("code", NbtElement.COMPOUND_TYPE), world)!!.list
                val stack = HexIotaTypes.LIST.deserialize(tag.getList("stack", NbtElement.COMPOUND_TYPE), world)!!.list.toList()
                val toCheck = listOf<CircleHelper.ItemSlot>().toMutableList()
                for (z in tag.getList("to_check", NbtElement.COMPOUND_TYPE)){
                    val slot = CircleHelper.ItemSlot.load(z as NbtCompound,world)
                    if (slot != null){
                        toCheck.add(slot)
                    }
                }
                return FrameGetItems(code, stack,toCheck)
            }

        }
    }

    // So. You can not make your own `OperatorSideEffect` (its sealed), so we have to make a *Rendered Spell* to spawn the items in
    private data class DumpDumbHexIsStupid(val itemSlotTup: Triple<CircleHelper.ItemSlot, Vec3d, Int>) : RenderedSpell{
        override fun cast(env: CastingEnvironment) {
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