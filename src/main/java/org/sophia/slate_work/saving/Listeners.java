package org.sophia.slate_work.saving;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.sophia.slate_work.blocks.impetus.ListeningImpetusEntity;

import java.util.ArrayList;
import java.util.List;

import static org.sophia.slate_work.Slate_work.chunk_listeners;

@SuppressWarnings("UnstableApiUsage")
public class Listeners {

    /** Returns a stored list of Listeners **/
    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    public static List<BlockPos> getListenersAroundPos(ServerWorld world, BlockPos centerPos){
        var centerChunk = new ChunkPos(centerPos);
        ArrayList<BlockPos> listeners = new ArrayList<>();
        for (var i = 0; i < 9; i++) { // gets all the chunks around the player in a 3x3 cube
            var x = (i % 3) -1;
            var z = ((int) (Math.floor(i/3.0)) % 3) -1;
            listeners.addAll(getListeners(world, centerChunk.x + x, centerChunk.z + z));
        }
        listeners.sort((a, b) -> { // cant have 2 BlockPoses at the same point
            if (a.getSquaredDistance(centerPos) > b.getSquaredDistance(centerPos)) {
                return 1;
            }
            return -1;
        });
        return listeners;
    }

    public static void saveListener(ServerWorld world, BlockPos pos){
        var chunk = world.getChunk(pos);
        var z = new ArrayList<>(chunk.getAttachedOrCreate(chunk_listeners)); // it *really* doesn't like messing with the given Array
        z.add(pos.toImmutable()); // Sometimes they were added as "mutable," so just to make sure they are all the same type
        System.out.println(z);
        chunk.setAttached(chunk_listeners, z);
        checkListeners(world, pos);
        chunk.setNeedsSaving(true); // Might not need this? But not a bad idea
    }

    public static void removeListener(ServerWorld world, BlockPos pos){
        var chunk = world.getChunk(pos);
        var z = new ArrayList<>(chunk.getAttachedOrCreate(chunk_listeners));
        z.remove(pos);
        chunk.setAttached(chunk_listeners, z);
        checkListeners(world, pos);
        chunk.setNeedsSaving(true);
    }

    // Checks if there are any invalid Listeners, and cleans them up
    public static void checkListeners(ServerWorld world, BlockPos pos){
        var z = getListeners(world, pos);
        z.removeIf(x -> !(world.getBlockEntity(x) instanceof ListeningImpetusEntity));
        world.getChunk(pos).setAttached(chunk_listeners, z);
    }

    public static List<BlockPos> getListeners(ServerWorld world, BlockPos pos){
        var chunk = world.getChunk(pos);
        return new ArrayList<>(chunk.getAttachedOrCreate(chunk_listeners));
    }

    public static List<BlockPos> getListeners(ServerWorld world, ChunkPos pos){
        var chunk = world.getChunk(pos.x, pos.z);
        return new ArrayList<>(chunk.getAttachedOrCreate(chunk_listeners));
    }

    public static List<BlockPos> getListeners(ServerWorld world, int x, int z){
        var chunk = world.getChunk(x, z);
        return new ArrayList<>(chunk.getAttachedOrCreate(chunk_listeners));
    }
}
