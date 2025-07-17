package org.sophia.slate_work.casting.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.api.misc.MediaConstants
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.item.ItemStack
import org.sophia.slate_work.casting.mishap.MishapNoStorageLoci
import org.sophia.slate_work.misc.CircleHelper.getStorage
import java.util.HashMap

object OpSortStorageLoci : ConstMediaAction{
    // In case the player fucks shit up, they can call this to quickly sort their vessels
    override val argc: Int
        get() = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()
        val storages = getStorage(env)
        if (storages.isEmpty())
            throw MishapNoStorageLoci(null)

        val returnList = HashMap<ItemVariant, Long>(1)
        for (z in storages){
            for (x in z.inventory){
                if (x.left == ItemVariant.of(ItemStack.EMPTY)) continue
                if (returnList.contains(x.left)){
                    returnList[x.left] = x.right + returnList.get(x.left)!!
                } else {
                    returnList.put(x.left, x.right)
                }
            }
            z.clear()
        }

        var storageI = 0
        var slotI = 0
        for (items in returnList){
            val storage = storages[storageI]
            if (slotI >= 16) {storageI++; slotI = 0}
            storage.setStack(slotI,items.key,items.value)
            slotI++
        }

        return listOf()
    }

    override val mediaCost: Long
        get() = MediaConstants.CRYSTAL_UNIT * 5

}