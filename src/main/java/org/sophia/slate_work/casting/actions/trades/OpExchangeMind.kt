package org.sophia.slate_work.casting.actions.trades

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.misc.MediaConstants
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import org.sophia.slate_work.blocks.TradeLoci
import org.sophia.slate_work.blocks.entities.StorageLociEntity
import org.sophia.slate_work.blocks.entities.TradeLociEntity
import org.sophia.slate_work.casting.mishap.MishapWrongBlock
import org.sophia.slate_work.misc.CircleHelper
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