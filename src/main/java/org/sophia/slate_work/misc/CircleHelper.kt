package org.sophia.slate_work.misc

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.putCompound
import miyucomics.hexpose.iotas.IdentifierIota
import miyucomics.hexpose.iotas.ItemStackIota
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.registry.Registries
import net.minecraft.server.world.ServerWorld
import org.sophia.slate_work.Slate_work.LOGGER
import org.sophia.slate_work.blocks.entities.StorageLociEntity

@Suppress("UnstableApiUsage")
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

    fun getOnlySlots(env: CircleCastEnv): List<ItemSlot> {
        val list = getStorage(env)
        val returnList: MutableList<ItemSlot> = mutableListOf()

        for (z in list){
            for (x in z.inventory) {
                if (!x.left.isBlank) {
                    returnList.add(ItemSlot(x.left,x.right,z))
                }
            }
        }
        return returnList
    }

    fun getOnlySlots(list: List<StorageLociEntity>): List<ItemSlot> {
        val returnList: MutableList<ItemSlot> = mutableListOf()

        for (z in list){
            for (x in z.inventory) {
                if (!x.left.isBlank) {
                    returnList.add(ItemSlot(x.left,x.right,z))
                }
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

    fun storeItems(env: CircleCastEnv, itemStack: ItemStack): Boolean {
        val list = getStorage(env)
        val hashMap = getLists(list)
        if (hashMap.contains(ItemVariant.of(itemStack.item, itemStack.nbt))) {
            val slot = hashMap.get(ItemVariant.of(itemStack.item, itemStack.nbt))!!
            val targ = slot.storageLociEntity.getSlot(slot.item)!! // *shouldn't* be null
            val item = slot.storageLociEntity.getStack(targ)
            item.right += itemStack.count
            slot.storageLociEntity.setStack(targ,item);
            return true
        }
        // If its not a known item yet...
        for (z in list) {
            val x = z.isFull
            if (x != -1) {
                z.setStack(x, ItemVariant.of(itemStack.item, itemStack.nbt), itemStack.count.toLong())
                return true
            }
        }
        return false
    }

    fun List<Iota>.getItemVariant(idx: Int, argc: Int = 0): ItemVariant? {
        val z = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
        if (z is IdentifierIota && Registries.ITEM.containsId(z.identifier)) {
            return ItemVariant.of(Registries.ITEM.get(z.identifier))
        } else if (z is ItemStackIota) {
            return ItemVariant.of(z.stack.item,z.stack.nbt)
        } else if (z is NullIota){
            return null
        }
        throw MishapInvalidIota.ofType(z, if (argc == 0) idx else argc - (idx + 1), "entity")
    }

    data class ItemSlot(val item: ItemVariant, var count: Long, val storageLociEntity: StorageLociEntity){
        fun save(): NbtCompound {
            val tempNBT = NbtCompound()
            tempNBT.putCompound("item",item.toNbt())
            tempNBT.putLong("count",count)
            tempNBT.put("entity", NbtHelper.fromBlockPos(storageLociEntity.pos))
            return tempNBT
        }
        companion object {

            @JvmStatic
            fun load(tempNBT: NbtCompound, world: ServerWorld): ItemSlot? {
                val item = ItemVariant.fromNbt(tempNBT.getCompound("item"))
                val count = tempNBT.getLong("count")
                val pos = NbtHelper.toBlockPos(tempNBT.getCompound("entity"))
                val entity = world.getBlockEntity(pos)

                if (entity !is StorageLociEntity) {
                    LOGGER.warning("Couldn't find Storage Locus at: " + pos.toShortString())
                    return null
                }

                return ItemSlot(item,count,entity)
            }
        }
    }
}