package org.sophia.slate_work.casting.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.util.math.BlockPos
import org.sophia.slate_work.blocks.entities.CraftingLociEntity
import org.sophia.slate_work.casting.mishap.MishapCraftingLocus
import org.sophia.slate_work.casting.mishap.MishapListLength
import org.sophia.slate_work.misc.CircleHelper.getItemVariant

object OpSetCraftingLoci : ConstMediaAction{
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list = args.getList(1,argc).toList()
        val target = args.getBlockPos(0,argc)

        if (list.size > 9) { // Since this a crafting table, we need a list smaller than 9
            throw MishapListLength(9,list.size)
        }
        val typeList = arrayOfNulls<ItemVariant>(9)

        var i = 0
        for (iota in list){
            typeList[i] = list.getItemVariant(i++,list.size)
        }

        val entity = env.world.getBlockEntity(BlockPos(target.x, target.y, target.z))
        if (entity !is CraftingLociEntity){
            throw MishapCraftingLocus(target)
        }
        entity.clear()
        i = 0
        for (item in typeList){
            if (item != null){
                entity.setStack(i,item.toStack())
            }
            i++
        }
        return listOf()
    }

    override val mediaCost: Long = MediaConstants.DUST_UNIT / 100
}