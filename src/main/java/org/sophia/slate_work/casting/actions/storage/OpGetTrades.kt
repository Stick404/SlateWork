package org.sophia.slate_work.casting.actions.storage

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import org.sophia.slate_work.blocks.entities.TradeLociEntity
import org.sophia.slate_work.casting.mishap.MishapWrongBlock
import org.sophia.slate_work.registries.BlockRegistry
import ram.talia.moreiotas.api.casting.iota.ItemStackIota

object OpGetTrades : ConstMediaAction  {
    override val argc: Int
        get() = 1

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): List<Iota> {
        val pos: BlockPos = args.getBlockPos(0, argc)
        val entity = env.world.getBlockEntity(pos)
        if (entity is TradeLociEntity) {
            var listOfIota: MutableList<Iota> = mutableListOf();
            for (offer in entity.offerList) {
                var index: MutableList<Iota> = mutableListOf();
                index.add(ItemStackIota.createFiltered(offer.adjustedFirstBuyItem));
                index.add(ItemStackIota.createFiltered(offer.secondBuyItem))
                index.add(ItemStackIota.createFiltered(offer.sellItem))
                index.add(DoubleIota((offer.uses.toDouble()/offer.maxUses.toDouble())))

                listOfIota.add(ListIota(index))
            }
            return listOf(ListIota(listOfIota))
        }

        throw MishapWrongBlock(pos, env.world.getBlockState(pos).block, BlockRegistry.TRADE_LOCI)
    }
}