package org.sophia.slate_work.misc


import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.chunk.ChunkStatus
import net.minecraft.world.chunk.WrapperProtoChunk
import org.sophia.slate_work.Slate_work

class ChunkScanning(var level: ServerWorld) {
    var chunks: Long2ObjectLinkedOpenHashMap<WrapperProtoChunk> = Long2ObjectLinkedOpenHashMap()

    /**
     *  This attempts to cache a chunk to the local [chunks]
     * @param ChunkPos the chunk to try to cache
     * @return If the function could cache the chunk or not
     */
    fun cacheChunk(chunk: ChunkPos): Boolean {
        val chunkLong = chunk.toLong()
        // We have the chunk already, so we can skip it
        if (chunks.contains(chunkLong)){
            return true
        }

        val z = level.chunkManager.getChunk(chunk.x,chunk.z, ChunkStatus.EMPTY,true)
        chunks.put(chunkLong, z as WrapperProtoChunk)
        return true

    }

    fun cacheChunk(chunk: Long): Boolean{
        return cacheChunk(ChunkPos(chunk))
    }

    fun getBlock(blockPos: BlockPos): BlockState? {
        val chunkPos = ChunkPos(blockPos).toLong()
        if (!cacheChunk(chunkPos)){
            return null
        }
        return chunks.get(chunkPos).getBlockState(blockPos)
    }

    fun getBlockEntity(blockPos: BlockPos): BlockEntity? {
        val chunkPos = ChunkPos(blockPos).toLong()
        if (!cacheChunk(chunkPos)){
            return null
        }
        return chunks.get(chunkPos).getBlockEntity(blockPos)
    }

    // Maybe not required, but still not a bad idea to have a Clear method
    fun clearCache(){
        chunks.clear()
    }

    // Might not be needed
    fun containsChunk(chunk: ChunkPos): Boolean {
        return chunks.contains(chunk.toLong())
    }
}