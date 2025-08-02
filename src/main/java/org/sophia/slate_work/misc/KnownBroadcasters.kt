package org.sophia.slate_work.misc

import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.sophia.slate_work.blocks.entities.BroadcasterLociEntity

object KnownBroadcasters {
    private var knownBroadcasters: HashMap<BlockPosDim, Iota> = HashMap()

    fun getOrLoad(world: ServerWorld, pos: BlockPos): Iota? {
        val dimPos = BlockPosDim(pos, world.dimensionKey.value)
        if (knownBroadcasters.contains(dimPos)) {
            return knownBroadcasters.get(dimPos)!!
        }

        val broadcaster = world.getBlockEntity(pos)
        if (broadcaster is BroadcasterLociEntity) {
            knownBroadcasters.put(BlockPosDim(pos, world.dimensionKey.value), broadcaster.iota)
            return broadcaster.iota
        }

        return GarbageIota()
    }

    fun setBroadcaster(broadcaster: BroadcasterLociEntity, iota: Iota){
        this.knownBroadcasters.put(BlockPosDim(broadcaster.pos, broadcaster.world!!.dimensionKey.value), iota)
    }

    fun removeBroadcaster(broadcaster: BroadcasterLociEntity){
        this.knownBroadcasters.remove(BlockPosDim(broadcaster.pos, broadcaster.world!!.dimensionKey.value))
    }

    fun clear(){
        this.knownBroadcasters.clear()
    }

    /**
     pos: BlockPos is the location of the block in the target dim

     dim: Identifer is the identifier of the target dim
     **/
    data class BlockPosDim(val pos: BlockPos, val dim: Identifier)
}