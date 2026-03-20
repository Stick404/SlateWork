package org.sophia.slate_work.casting.actions.storage

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.getIntBetween
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import org.sophia.slate_work.blocks.entities.CraftingLociEntity
import org.sophia.slate_work.casting.mishap.MishapWrongBlock
import org.sophia.slate_work.registries.BlockRegistry

object OpSetCraftingCount : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): List<Iota> {
        val block = args.getBlockPos(0, argc)
        val count = args.getIntBetween(1, 1, Int.MAX_VALUE, argc)
        val blockEntity = env.world.getBlockEntity(block)
        if (blockEntity is CraftingLociEntity) {
            blockEntity.craftCount = count
        } else {
            throw MishapWrongBlock(block, BlockRegistry.CRAFTING_LOCI, env.world.getBlockState(block).block)
        }
        return listOf()
    }
}