package org.sophia.slate_work.misc

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.putCompound
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtIntArray
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import org.sophia.slate_work.Slate_work.LOGGER
import org.sophia.slate_work.blocks.entities.SentinelLociEntity
import org.sophia.slate_work.blocks.entities.StorageLociEntity
import ram.talia.moreiotas.api.casting.iota.ItemStackIota
import ram.talia.moreiotas.api.casting.iota.ItemTypeIota

object CircleHelper {
    fun getSentLoci(env: CircleCastEnv): List<SentinelLociEntity> {
        val list: ArrayList<SentinelLociEntity> = ArrayList()
        val nbt = env.circleState().currentImage.userData.getList("sentinel_loci", NbtElement.INT_ARRAY_TYPE.toInt())

        for (temp in nbt){
            val z = temp as NbtCompound
            val entity = env.world.getBlockEntity(NbtHelper.toBlockPos(z, "pos").get())
            if (entity is SentinelLociEntity){
                list.add(entity)
            }
        }
        return list
    }

    fun getStorage(env: CircleCastEnv): List<StorageLociEntity> {
        val list: ArrayList<StorageLociEntity> = ArrayList()
        val nbt = env.circleState().currentImage.userData.getList("storage_loci", NbtElement.INT_ARRAY_TYPE.toInt())

        for (itemTemp in nbt){
            val z = itemTemp as NbtIntArray
            val entity = env.world.getBlockEntity(BlockPos(z[0].intValue(), z[1].intValue(), z[2].intValue()))
            if (entity is StorageLociEntity)
                list.add(entity)
        }

        return list
    }

    fun getLists(env: CircleCastEnv): HashMap<ItemVariant, ItemSlot> {
        val list = getStorage(env)
        val returnList = HashMap<ItemVariant, ItemSlot>()
        for (z in list){
            for (x in z.inventory){
                returnList[x.resource] = ItemSlot(x.resource,x.amount, z.pos)
            }
        }
        return returnList
    }

    fun getOnlySlots(env: CircleCastEnv): List<ItemSlot> {
        val list = getStorage(env)
        val returnList: MutableList<ItemSlot> = mutableListOf()

        for (z in list){
            for (x in z.inventory) {
                if (!x.resource.isBlank) {
                    returnList.add(ItemSlot(x.resource,x.amount, z.pos))
                }
            }
        }
        return returnList
    }

    fun getOnlySlots(list: List<StorageLociEntity>): List<ItemSlot> {
        val returnList: MutableList<ItemSlot> = mutableListOf()

        for (z in list){
            for (x in z.inventory) {
                if (!x.resource.isBlank) {
                    returnList.add(ItemSlot(x.resource,x.amount, z.pos))
                }
            }
        }
        return returnList
    }

    fun getLists(list: List<StorageLociEntity>): HashMap<ItemVariant, ItemSlot> {
        val returnList = HashMap<ItemVariant, ItemSlot>()
        for (z in list){
            for (x in z.inventory){
                returnList[x.resource] = ItemSlot(x.resource,x.amount, z.pos)
            }
        }
        return returnList
    }

    fun storeItems(env: CircleCastEnv, itemStack: ItemStack): Boolean {
        val list = getStorage(env)
        val hashMap = getLists(list)
        if (hashMap.contains(ItemVariant.of(itemStack.item, itemStack.componentChanges))) {
            val slot = hashMap[ItemVariant.of(itemStack.item, itemStack.componentChanges)]!!

            val entity = env.world.getBlockEntity(slot.pos)
            if (entity !is StorageLociEntity){
                return false
            }

            val targ = entity.getSlot(slot.item)!! // *shouldn't* be null
            val item = entity.getStack(targ)
            item.right += itemStack.count
            entity.setStack(targ,item)
            return true
        }
        // If its not a known item yet...
        for (z in list) {
            val x = z.isFull
            if (x != -1) {
                z.setStack(x, ItemVariant.of(itemStack.item, itemStack.componentChanges), itemStack.count.toLong())
                return true
            }
        }
        return false
    }

    fun List<Iota>.getItemVariant(idx: Int, argc: Int = 0): ItemVariant? {
        val z = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
        if (z is ItemTypeIota) {
            return z.either.map({ ItemVariant.of { it } }) { ItemVariant.of{ BlockItem(it, Item.Settings()) } }
        } else if (z is ItemStackIota) {
            return ItemVariant.of(z.itemStack.item,z.itemStack.componentChanges)
        } else if (z is NullIota){
            return null
        }
        throw MishapInvalidIota.ofType(z, if (argc == 0) idx else argc - (idx + 1), "entity")
    }

    data class ItemSlot(val item: ItemVariant, var count: Long, val pos: BlockPos)
}