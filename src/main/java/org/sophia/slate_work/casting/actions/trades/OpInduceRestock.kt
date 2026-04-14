package org.sophia.slate_work.casting.actions.trades

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.village.TradeOffer
import org.sophia.slate_work.blocks.entities.TradeLociEntity
import org.sophia.slate_work.casting.mishap.MishapWrongBlock
import org.sophia.slate_work.registries.BlockRegistry

object OpInduceRestock : SpellAction {
    override val argc: Int
        get() = 0

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): SpellAction.Result {
        val blockPos = args.getBlockPos(0, argc)
        env.assertPosInRange(blockPos)
        val block = env.world.getBlockEntity(blockPos)
        if (block !is TradeLociEntity) {
            throw MishapWrongBlock(blockPos, BlockRegistry.TRADE_LOCI, env.world.getBlockState(blockPos).block)
        }

        return SpellAction.Result(
            Spell(block),
            MediaConstants.CRYSTAL_UNIT*5,
            listOf(ParticleSpray.burst(block.pos.toCenterPos(),1.0))
        )
    }

    private data class Spell(val block: TradeLociEntity) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            block.offerList.forEach { a: TradeOffer? ->
                a!!.updateDemandBonus()
                a.resetUses()
                block.lastRestockTime = env.world.time
                block.markDirty()
            }
        }
    }
}