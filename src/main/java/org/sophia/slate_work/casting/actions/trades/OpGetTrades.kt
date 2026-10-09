package org.sophia.slate_work.casting.actions.trades

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import net.minecraft.util.math.BlockPos
import org.sophia.slate_work.blocks.entities.TradeLociEntity
import org.sophia.slate_work.casting.mishap.MishapWrongBlock
import org.sophia.slate_work.registries.SlateWorksBlockRegistry
import ram.talia.moreiotas.api.casting.iota.ItemStackIota

object OpGetTrades : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): List<Iota> {
        val pos: BlockPos = args.getBlockPos(0, argc)
        env.assertVecInRange(pos.toCenterPos())
        val entity = env.world.getBlockEntity(pos)
        if (entity is TradeLociEntity) {
            val listOfIota: MutableList<Iota> = mutableListOf();
            for (offer in entity.offerList) {
                val index: MutableList<Iota> = mutableListOf();

                index.add(ItemStackIota.createFiltered(offer.firstBuyItem.itemStack));
                offer.secondBuyItem.ifPresent {
                    index.add(ItemStackIota.createFiltered(it.itemStack))
                }
                index.add(ItemStackIota.createFiltered(offer.sellItem))
                index.add(DoubleIota((offer.uses.toDouble() / offer.maxUses.toDouble())))

                listOfIota.add(ListIota(index))
            }
            return listOf(ListIota(listOfIota))
        }

        throw MishapWrongBlock(pos, SlateWorksBlockRegistry.TRADE_LOCI, env.world.getBlockState(pos).block)
    }
}