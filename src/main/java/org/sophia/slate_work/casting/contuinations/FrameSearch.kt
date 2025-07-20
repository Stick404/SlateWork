package org.sophia.slate_work.casting.contuinations

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.api.utils.putList
import at.petrak.hexcasting.api.utils.serializeToNBT
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import miyucomics.hexpose.iotas.ItemStackIota
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import org.sophia.slate_work.misc.CircleHelper

class FrameSearch(
    val code: SpellList,
    val baseStack: List<Iota>,
    val toCheck: MutableList<CircleHelper.ItemSlot>,
    var toReturn: MutableList<Triple<CircleHelper.ItemSlot, Vec3d, Int>>
) : ContinuationFrame {
    override val type: ContinuationFrame.Type<*>
        get() = TYPE

    override fun breakDownwards(stack: List<Iota>): Pair<Boolean, List<Iota>> {
        return true to listOf()
    }

    // Kind of copies what Thoth's (FrameForEach) does
    override fun evaluate(continuation: SpellContinuation, level: ServerWorld, harness: CastingVM): CastResult {
        val stack = baseStack.toMutableList()
        val slot = toCheck.removeFirstOrNull()

        val realStack = harness.image.stack.reversed()

        if (slot != null){
            stack.add(ItemStackIota(slot.item.toStack(if (slot.count > Int.MAX_VALUE) Int.MAX_VALUE else slot.count.toInt())))
            if (realStack.getBool(0, 3)){
                val amount = realStack.getInt(1,3)
                val pos = realStack.getVec3(2,3)
                harness.env.assertVecInRange(pos)
                toReturn.add(Triple(slot, pos, amount))
            }
        }

        val cont = if (slot != null) {
            continuation
                .pushFrame(FrameSearch(code,baseStack,toCheck,toReturn))
                .pushFrame(FrameEvaluate(code,true))
        } else {
            continuation
        }

        return CastResult(
            ListIota(code),
            cont,
            harness.image.withUsedOp().copy(stack = stack),
            listOf(), // Do *not* do any Side Effects
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

        val listFound = NbtList()
        for (z in toReturn){
            val tempCompound = NbtCompound()

            tempCompound.putCompound("pos", z.second.serializeToNBT())
            tempCompound.putInt("amount",z.third)
            tempCompound.putCompound("slot",z.first.save())
        }
        compound.putList("found", listFound)

        return compound
    }

    override fun size(): Int = baseStack.size + toReturn.size

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameSearch> = object : ContinuationFrame.Type<FrameSearch> {
            override fun deserializeFromNBT(tag: NbtCompound, world: ServerWorld): FrameSearch? {
                val code = HexIotaTypes.LIST.deserialize(tag.getList("code", NbtElement.COMPOUND_TYPE), world)!!.list
                val stack = HexIotaTypes.LIST.deserialize(tag.getList("stack", NbtElement.COMPOUND_TYPE), world)!!.list.toList()
                val toCheck = listOf<CircleHelper.ItemSlot>().toMutableList()
                for (z in tag.getList("to_check", NbtElement.COMPOUND_TYPE)){
                    val slot = CircleHelper.ItemSlot.load(z as NbtCompound,world)
                    if (slot != null){
                        toCheck.add(slot)
                    }
                }

                val found = listOf<Triple<CircleHelper.ItemSlot, Vec3d, Int>>().toMutableList()
                for (z in tag.getList("found", NbtElement.COMPOUND_TYPE)){
                    val compound = z as NbtCompound
                    val pos = NbtHelper.toBlockPos(compound.getCompound("pos"))
                    found.add(
                        Triple(
                            CircleHelper.ItemSlot.load(compound.getCompound("slot"), world)!!,
                            Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()),
                            0
                        )
                    )
                }
                return FrameSearch(code, stack,toCheck,found)
            }

        }
    }
}