package org.sophia.slate_work.casting;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.env.CircleMishapEnv;
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.casting.mishap.SearchingMishapEnv;

import java.util.List;
import java.util.function.Predicate;

public class SearchingBasedEnv extends CastingEnvironment {
    final CastingEnvironment parent;

    public SearchingBasedEnv(CastingEnvironment parentEnv) {
        super(parentEnv.getWorld());
        parent = parentEnv;
    }

    @Override
    public @Nullable LivingEntity getCastingEntity() {
        return null;
    }

    @Override
    public MishapEnvironment getMishapEnvironment() {
        return new SearchingMishapEnv(this.world);
    }

    @Override
    public Vec3d mishapSprayPos() {
        return parent.mishapSprayPos().add(new Vec3d(0d,1d,0d));
    }

    @Override
    protected long extractMediaEnvironment(long l, boolean b) {
        return 0;
    }

    @Override
    protected boolean isVecInRangeEnvironment(Vec3d vec3d) {
        return false;
    }

    @Override
    protected boolean hasEditPermissionsAtEnvironment(BlockPos blockPos) {
        return false;
    }

    @Override
    public Hand getCastingHand() {
        return Hand.MAIN_HAND;
    }

    @Override
    protected List<ItemStack> getUsableStacks(StackDiscoveryMode stackDiscoveryMode) {
        return List.of();
    }

    @Override
    protected List<HeldItemInfo> getPrimaryStacks() {
        return List.of();
    }

    @Override
    public boolean replaceItem(Predicate<ItemStack> predicate, ItemStack itemStack, @Nullable Hand hand) {
        return false;
    }

    @Override
    public FrozenPigment getPigment() {
        return FrozenPigment.DEFAULT.get();
    }

    @Override
    public @Nullable FrozenPigment setPigment(@Nullable FrozenPigment frozenPigment) {
        return null;
    }

    @Override
    public void produceParticles(ParticleSpray particleSpray, FrozenPigment frozenPigment) {
    }

    @Override
    public void printMessage(Text text) {
        this.parent.printMessage(text);
    }
}
