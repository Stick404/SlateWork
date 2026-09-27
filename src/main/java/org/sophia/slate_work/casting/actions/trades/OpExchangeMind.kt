package org.sophia.slate_work.casting.actions.trades

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.text.Text
import org.sophia.slate_work.blocks.entities.TradeLociEntity
import org.sophia.slate_work.casting.mishap.MishapWrongBlock
import org.sophia.slate_work.registries.BlockRegistry

object OpExchangeMind : SpellAction {
    override val argc: Int
        get() = 2

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): SpellAction.Result {
        val entity = args.getEntity(1, argc)
        val block = args.getBlockPos(0, argc)

        if (entity !is VillagerEntity){
            throw MishapBadEntity(entity, Text.translatable("entity.minecraft.villager"))
        }
        val blockEntity = env.world.getBlockEntity(block)
        if (blockEntity !is TradeLociEntity) {
            throw MishapWrongBlock(block, BlockRegistry.TRADE_LOCI, env.world.getBlockState(block).block)
        }

        return SpellAction.Result(
            Spell(entity, blockEntity),
            MediaConstants.CRYSTAL_UNIT,
            listOf(ParticleSpray.burst(entity.pos,1.0))
        )
    }

    private data class Spell(val entity: VillagerEntity, val block: TradeLociEntity) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (!entity.isAlive)
                return
            // Slurp 'em up them villagers
            // Yum


                    // Yum.
            block.slurpVillager(entity)
        }
    }
}