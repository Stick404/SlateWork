package org.sophia.slate_work.casting.contuinations

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.SpellList
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
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.api.utils.putList
import at.petrak.hexcasting.api.utils.serializeToNBT
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.block.Block
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtList
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.sophia.slate_work.blocks.entities.BlockBreakLociEntity
import org.sophia.slate_work.casting.mishap.MishapSpellCircleMedia
import org.sophia.slate_work.misc.CircleHelper
import ram.talia.moreiotas.api.casting.iota.ItemTypeIota

@Suppress("DATA_CLASS_INVISIBLE_COPY_USAGE_WARNING")
class FrameBreakBlockLoci(
    val code: SpellList,
    val blockBreakingLocus: BlockPos,
    val baseStack: List<Iota>,
    val toCheck: MutableList<BlockPos>,
    val oldReturn: BlockPos?,
    var isFirst: JankyMaybe,
    var itemStack: ItemStack // Clone this when using!
) : ContinuationFrame {
    override val type: ContinuationFrame.Type<*>
        get() = TYPE

    override fun breakDownwards(stack: List<Iota>): Pair<Boolean, List<Iota>> {
        return true to listOf()
    }

    // Kind of copies what Thoth's (FrameForEach) does
    override fun evaluate(continuation: SpellContinuation, level: ServerWorld, harness: CastingVM): CastResult {
        val stack = baseStack.toMutableList()
        val slot = if (isFirst != JankyMaybe.LAST && toCheck.isNotEmpty()){
            toCheck.removeFirst()
        } else {
            isFirst = JankyMaybe.LAST
            BlockPos.ORIGIN // Safe because when `LAST` is ran, it doesn't use `slot`
        }

        val realStack = harness.image.stack.reversed()
        val sideEffect: MutableList<OperatorSideEffect> = mutableListOf()

        if (isFirst != JankyMaybe.FIRST && oldReturn != null){
            try {
                val entity = level.getBlockEntity(blockBreakingLocus)
                if (entity !is BlockBreakLociEntity) {
                    throw MishapBadLocation(blockBreakingLocus.toCenterPos()) // This isn't great, but should *never* be reached
                }

                if (harness.env !is CircleCastEnv) {
                    throw MishapNoSpellCircle() // Chloe I know you are reading this. No.
                }

                val enchantments = EnchantmentHelper.fromNbt(entity.enchantments)
                val fortuneCost = enchantments[Enchantments.FORTUNE]?.toLong() ?: 0 // Since the item may not have Fortune
                val efficiencyMult = (enchantments[Enchantments.EFFICIENCY]?.toLong() ?: 0) +2
                val silkTouchCost: Long = if (enchantments.containsKey(Enchantments.SILK_TOUCH)) { // Nor SilkTouch
                    1
                } else {
                    0
                }

                // Uhh, random bullshit go
                // TODO: This `oldReturn` *may* break. Watch out!
                val isCheap: Boolean = level.getBlockState(oldReturn).streamTags()
                    .anyMatch { a: TagKey<Block?>? -> a == HexTags.Blocks.CHEAP_TO_BREAK_BLOCK }
                val cheapCost = if (isCheap) MediaConstants.DUST_UNIT / 100 else MediaConstants.DUST_UNIT / 8
                val cost =
                    ((cheapCost + (silkTouchCost * MediaConstants.SHARD_UNIT) + (fortuneCost * MediaConstants.DUST_UNIT)) / (efficiencyMult * 0.5)).toLong()


                // Okay so. Theres the source cost, then its added by either Silk Touch (an extra shard), or added Fortune*a shard.
                // This means Fortune 3 (highest base game) is an extra 3 shards; feels fare since its a locus/needs the block in front of it.
                // Then, the whole cost is divided by efficiency*0.5; meaning highest cost reduce (base game) is 2.5 times.
                // So, the "best" locus would cost... 1.25 dust (Eff 5, Fort 3)

                if (realStack.getBool(0, 0)){
                    val extracted: Long = harness.env.extractMedia(cost, false)
                    // If we fail to extract Media at any point, shrimply die
                    if (0L != extracted) {
                        throw MishapSpellCircleMedia(extracted, blockBreakingLocus)
                    }

                    oldReturn?.let {
                        sideEffect.add(OperatorSideEffect.AttemptSpell(DumpDumbHexIsStupid(
                            it, itemStack
                        )))
                    }
                }
            } catch (e : Mishap){
                // TODO: Change this translation!
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

        if (this.toCheck.isEmpty() && this.isFirst != JankyMaybe.LAST){
            this.isFirst = JankyMaybe.PENULTIMATE
        }

        val cont = if (isFirst != JankyMaybe.LAST){

            stack.add(ItemTypeIota(level.getBlockState(slot).block))

             when (isFirst){
                JankyMaybe.PENULTIMATE -> {
                    continuation
                        .pushFrame(FrameBreakBlockLoci(code, blockBreakingLocus, baseStack, toCheck, slot, JankyMaybe.LAST, itemStack))
                        .pushFrame(FrameEvaluate(code,true))
                }
                else -> { // When FIRST or RUNNING push the frame
                    continuation
                        .pushFrame(FrameBreakBlockLoci(code, blockBreakingLocus, baseStack,toCheck,slot, JankyMaybe.RUNNING, itemStack))
                        .pushFrame(FrameEvaluate(code,true))
                }
            }
        } else continuation

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
        compound.putCompound("loci_block", NbtHelper.fromBlockPos(blockBreakingLocus))
        compound.putList("code", code.serializeToNBT() as NbtList)

        val listCheck = NbtList()
        for (z in toCheck){
            listCheck.add(NbtHelper.fromBlockPos(z))
        }
        compound.putList("to_check",listCheck)
        compound.putCompound("old_pos", NbtHelper.fromBlockPos(this.oldReturn))
        compound.putString("jank_maybe", this.isFirst.name)
        return compound
    }

    override fun size(): Int = baseStack.size

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameBreakBlockLoci> = object : ContinuationFrame.Type<FrameBreakBlockLoci> {
            override fun deserializeFromNBT(tag: NbtCompound, world: ServerWorld): FrameBreakBlockLoci {
                val code = HexIotaTypes.LIST.deserialize(tag.getList("code", NbtElement.COMPOUND_TYPE), world)!!.list
                val loci = NbtHelper.toBlockPos(tag.getCompound("loci_block"))
                val stack = HexIotaTypes.LIST.deserialize(tag.getList("stack", NbtElement.COMPOUND_TYPE), world)!!.list.toList()

                val toCheck = listOf<BlockPos>().toMutableList()
                for (z in tag.getList("to_check", NbtElement.COMPOUND_TYPE)){
                    val slot = NbtHelper.toBlockPos(z as NbtCompound)
                    if (slot != null){
                        toCheck.add(slot)
                    }
                }

                val oldReturn = NbtHelper.toBlockPos(tag.getCompound("old_pos"))
                val stepEval = JankyMaybe.valueOf(tag.getString("jank_maybe"))

                val itemStack = ItemStack.fromNbt(tag.getCompound("item_stack"))
                // If it is a normal "running," then restart it as a "first"
                return FrameBreakBlockLoci(code, loci, stack,toCheck, oldReturn,if (stepEval == JankyMaybe.RUNNING) JankyMaybe.FIRST else stepEval, itemStack)
            }

        }
    }

    // So. You can not make your own `OperatorSideEffect` (its sealed), so we have to make a *Rendered Spell* to spawn the items in
    private data class DumpDumbHexIsStupid(val pos: BlockPos, val itemStack: ItemStack) : RenderedSpell{
        override fun cast(env: CastingEnvironment) {
            val state = env.world.getBlockState(pos)
            val entity = env.world.getBlockEntity(pos)

            Block.getDroppedStacks(state, env.world, pos, entity, env.castingEntity, itemStack.copy())
                .stream().map { a: ItemStack ->
                    net.minecraft.util.Pair(
                        a,
                        pos.toCenterPos()
                    )
                }.forEach { item ->
                    if (!CircleHelper.storeItems(env as CircleCastEnv, item.left)){
                        // If it fails to store, spit item out
                        val center = item.getRight();
                        val itemEntity =
                            ItemEntity(env.world, center.getX(), center.getY(), center.getZ(), item.getLeft());
                        itemEntity.setToDefaultPickupDelay();
                        env.world.spawnEntity(itemEntity);
                    }
                }
                env.world.breakBlock(pos, false)
        }
    }
}