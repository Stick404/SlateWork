package org.sophia.slate_work.misc

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import miyucomics.hexpose.iotas.IdentifierIota
import miyucomics.hexpose.iotas.ItemStackIota
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.registry.Registries
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

    fun List<Iota>.getItemVariant(idx: Int, argc: Int = 0): ItemVariant {
        val z = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
        if (z is IdentifierIota && Registries.ITEM.containsId(z.identifier)) {
            return ItemVariant.of(Registries.ITEM.get(z.identifier))
        } else if (z is ItemStackIota) {
            return ItemVariant.of(z.stack.item,z.stack.nbt)
        }
        throw MishapInvalidIota.ofType(z, if (argc == 0) idx else argc - (idx + 1), "entity")
    }

    data class ItemSlot(val item: ItemVariant, var count: Long, val storageLociEntity: StorageLociEntity)
}