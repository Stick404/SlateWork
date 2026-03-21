package org.sophia.slate_work.casting.actions.storage

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import org.sophia.slate_work.blocks.entities.CraftingLociEntity
import org.sophia.slate_work.casting.mishap.MishapWrongBlock
import org.sophia.slate_work.registries.BlockRegistry

object OpGetCraftingCount : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): List<Iota> {
        val block = args.getBlockPos(0, argc)
        env.assertPosInRange(block)
        val blockEntity = env.world.getBlockEntity(block)
        if (blockEntity is CraftingLociEntity) {
            return listOf(DoubleIota(blockEntity.craftCount.toDouble()))
        } else {
            throw MishapWrongBlock(block, BlockRegistry.CRAFTING_LOCI, env.world.getBlockState(block).block)
        }
    }
}