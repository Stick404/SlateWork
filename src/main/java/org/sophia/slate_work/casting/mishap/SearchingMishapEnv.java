package org.sophia.slate_work.casting.mishap;

import at.petrak.hexcasting.api.casting.eval.MishapEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

// How will you Mishap with no Media? And if you do, you deserve it
public class SearchingMishapEnv extends MishapEnvironment {
    public SearchingMishapEnv(ServerWorld world) {
        super(world, null);
    }

    @Override
    public void yeetHeldItemsTowards(Vec3d vec3d) {}

    @Override
    public void dropHeldItems() {}

    @Override
    public void drown() {}

    @Override
    public void damage(float v) {}

    @Override
    public void removeXp(int i) {}

    @Override
    public void blind(int i) {}
}
