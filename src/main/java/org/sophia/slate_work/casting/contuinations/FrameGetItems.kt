package org.sophia.slate_work.casting.contuinations

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.getPositiveInt
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import com.mojang.serialization.MapCodec
import net.fabricmc.fabric.impl.transfer.transaction.TransactionManagerImpl
import net.minecraft.entity.ItemEntity
import net.minecraft.nbt.NbtOps
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import org.sophia.slate_work.blocks.entities.StorageLociEntity
import org.sophia.slate_work.misc.CircleHelper
import org.sophia.slate_work.misc.SlateWorksCodecs
import ram.talia.moreiotas.api.casting.iota.ItemStackIota
import java.util.Objects

@Suppress("UnstableApiUsage", "DATA_CLASS_INVISIBLE_COPY_USAGE_WARNING")
class FrameGetItems(
    val code: TreeList<Iota>,
    val baseStack: TreeList<Iota>,
    val toCheck: MutableList<CircleHelper.ItemSlot>,
    val oldReturn: CircleHelper.ItemSlot?,
    var isFirst: JankyMaybe
) : ContinuationFrame {
    override val type: ContinuationFrame.Type<*>
        get() = TYPE

    override fun breakDownwards(stack: TreeList<Iota>): Pair<Boolean, TreeList<Iota>> {
        return true to stack
    }

    // Kind of copies what Thoth's (FrameForEach) does
    override fun evaluate(continuation: SpellContinuation, level: ServerWorld, harness: CastingVM): CastResult {
        println("Starting eval on GetItems")
        var stack = harness.image.stack
        println("Checking stack: $stack")
        val slot = if (isFirst != JankyMaybe.LAST && toCheck.isNotEmpty()){
            toCheck.removeFirst()
        } else {
            isFirst = JankyMaybe.LAST
            null
        }

        val sideEffect: MutableList<OperatorSideEffect> = mutableListOf()

        if (isFirst != JankyMaybe.FIRST && oldReturn != null){
            try {
                if (harness.env !is CircleCastEnv) {
                    throw MishapNoSpellCircle() // Chloe I know you are reading this. No.
                }

                println("Getting bool")
                val rev = stack.reversedVec();
                if (rev.getBool(0, 2)){
                    val pos = rev.getVec3(1,2)
                    val amount = rev.getPositiveInt(2,2)
                    harness.env.assertVecInRange(pos)
                    println("Added side effect!")
                    sideEffect.add(OperatorSideEffect.AttemptSpell(DumpDumbHexIsStupid(
                        Triple(oldReturn,pos,amount)
                    )))
                }
            } catch (e : Mishap){
                sideEffect.add(OperatorSideEffect.DoMishap(e, Mishap.Context(null,
                    Text.translatable("hexcasting.action.slate_work:get_item"))))
                println("Mishap")
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

        if (this.toCheck.isEmpty() && this.isFirst != JankyMaybe.LAST){
            this.isFirst = JankyMaybe.PENULTIMATE
        }

        val cont = if (isFirst != JankyMaybe.LAST){
            val itemStack = slot!!.item.toStack(if (slot.count > Int.MAX_VALUE) Int.MAX_VALUE else slot.count.toInt())
            stack = baseStack.appended(SlateWorksCodecs.makeIota(itemStack, level.registryManager));

            println("Doing wonky check")
            when (isFirst){
               JankyMaybe.PENULTIMATE -> {
                   continuation
                       .pushFrame(FrameGetItems(code, baseStack, toCheck, slot, JankyMaybe.LAST))
                       .pushFrame(FrameEvaluate(code,true))
               }
               else -> { // When FIRST or RUNNING push the frame
                   continuation
                       .pushFrame(FrameGetItems(code, baseStack, toCheck,slot, JankyMaybe.RUNNING))
                       .pushFrame(FrameEvaluate(code,true))
               }
            }
        } else {
            stack = baseStack
            continuation
        }

        println("Cast that thang with stack $stack")
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
        val TYPE: ContinuationFrame.Type<FrameGetItems> = object : ContinuationFrame.Type<FrameGetItems> {

            override fun codec(): MapCodec<FrameGetItems> {
                println("Wow look at me! I am getting the MapCodec")
                return SlateWorksCodecs.FRAME_GET_ITEMS_MAP_CODEC
            }

            override fun streamCodec(): PacketCodec<RegistryByteBuf, FrameGetItems> {
                println("Wow look at me! I am getting the StreamCodec")
                return SlateWorksCodecs.FRAME_GET_ITEMS_PACKET_CODEC
            }

        }
    }

    // So. You can not make your own `OperatorSideEffect` (its sealed), so we have to make a *Rendered Spell* to spawn the items in
    private data class DumpDumbHexIsStupid(val itemSlotTup: Triple<CircleHelper.ItemSlot, Vec3d, Int>) : RenderedSpell{
        override fun cast(env: CastingEnvironment) {
            val itemSlot = itemSlotTup.first
            val vec = itemSlotTup.second
            val amount = itemSlotTup.third.toLong()

            val trans = TransactionManagerImpl().openOuter()
            val entity = env.world.getBlockEntity(itemSlot.pos)
            if (entity !is StorageLociEntity) {
                return
            }

            val extracted = entity.extract(itemSlot.item, amount, trans)
            trans.addCloseCallback { _, result ->
                if (result.wasCommitted()) {
                    val stack = itemSlot.item.toStack(extracted.toInt()) //(, extracted.toInt(), itemSlot.item.components)

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
            trans.commit()
        }
    }
}