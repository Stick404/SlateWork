package org.sophia.slate_work.casting.actions

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.component.ComponentChanges
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import org.sophia.slate_work.blocks.AbstractSlate.FACING
import org.sophia.slate_work.blocks.entities.BlockBreakLociEntity
import org.sophia.slate_work.casting.contuinations.FrameBreakBlockLoci
import org.sophia.slate_work.casting.contuinations.JankyMaybe
import kotlin.math.roundToInt

/**
 * Okay so, its likely important to give a bit of context here.
 *
 * For the Bombastic Extractor ([org.sophia.slate_work.blocks.BlockBreakLoci]), it needs to push a custom Continuation
 * ([FrameBreakBlockLoci]). But as far as I can tell, this can't really be done *in* the Locus; thus this pattern.
 * Also, this pattern has a randomly generated Signature on every reload. So it does kind of exist and players
 * could draw it, but it would be inconsistent and wouldn't work after server restart.
 */
object OpIAmSoSorryForMyCrimesHexxyForgiveMe : Action {
    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ): OperationResult {
        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()

        var stack = image.stack
        val revStack = stack.reversedVec()
        val hex = revStack.getList(0, 1)
        stack = stack.init()

        val pos = env.circleState().currentPos;
        val locus = env.world.getBlockState(pos)
        val facing = locus.get(FACING)
        val entity = env.world.getBlockEntity(pos)

        if (entity !is BlockBreakLociEntity){
            throw MishapBadLocation(pos.toCenterPos())
        }

        val bounds = env.circleState().bounds
        val offset = when(facing) {
            Direction.NORTH -> pos.z - bounds.enclosingBox().minZ
            Direction.SOUTH -> bounds.enclosingBox().maxZ - pos.z
            Direction.EAST -> pos.x - bounds.enclosingBox().minX
            Direction.WEST -> bounds.enclosingBox().maxX - pos.x
            Direction.UP -> bounds.enclosingBox().maxY - pos.y
            Direction.DOWN -> pos.y - bounds.enclosingBox().maxY
        }

        val blocks: MutableList<BlockPos> =
            BlockPos.stream(pos.offset(facing), pos.offset(facing, offset.roundToInt()))
                .map { block -> block.toImmutable() }
                .filter { block -> env.isVecInAmbit(block.toCenterPos()) }.toList().toMutableList()

        val fakePick = ItemStack(Items.NETHERITE_PICKAXE)
        fakePick.applyChanges(ComponentChanges.builder().add(
            DataComponentTypes.ENCHANTMENTS, entity.enchantments).build())

        val frame = FrameBreakBlockLoci(hex, pos,stack,
            blocks.toMutableList(), null, JankyMaybe.FIRST, fakePick)
        val image2 = image.withUsedOp().copy(stack = stack)


        return OperationResult(image2,
            listOf(),
            continuation.pushFrame(frame), HexEvalSounds.SPELL.get())
    }
}