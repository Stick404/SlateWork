package org.sophia.slate_work.misc

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import org.sophia.slate_work.blocks.StorageLociEntity

object CircleHelper {
    fun getStorage(env: CircleCastEnv): List<StorageLociEntity> {
        val list: ArrayList<StorageLociEntity> = ArrayList()
        val nbt = env.circleState().currentImage.userData.getList("storage_loci", NbtElement.COMPOUND_TYPE.toInt())

        for (itemTemp in nbt){
            val z = itemTemp as NbtCompound
            val entity = env.world.getBlockEntity(NbtHelper.toBlockPos(z))
            if (entity is StorageLociEntity)
                list.add(entity)
        }

        return list.reversed() //So we "find" the first storages last
    }

    fun getLists(env: CircleCastEnv): HashMap<ItemVariant, ItemSlot> {
        val list = getStorage(env)
        val returnList = HashMap<ItemVariant, ItemSlot>()
        for (z in list){
            for (x in z.inventory){
                returnList.put(x.left, ItemSlot(x.left,x.right,z))
            }
        }
        return returnList
    }

    fun getLists(list: List<StorageLociEntity>): HashMap<ItemVariant, ItemSlot> {
        val returnList = HashMap<ItemVariant, ItemSlot>()
        for (z in list){
            for (x in z.inventory){
                returnList.put(x.left, ItemSlot(x.left,x.right,z))
            }
        }
        return returnList
    }

    data class ItemSlot(val item: ItemVariant, var count: Long, val storageLociEntity: StorageLociEntity)
}