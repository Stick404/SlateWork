package org.sophia.slate_work.casting;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.casting.mishap.SearchingMishapEnv;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SearchingBasedEnv extends CastingEnvironment {
    final CircleCastEnv parent;

    public SearchingBasedEnv(CircleCastEnv parentEnv) {
        super(parentEnv.getWorld());
        parent = parentEnv;
    }

    @Override
    public @Nullable LivingEntity getCastingEntity() {
        return parent.getCastingEntity();
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
        return this.parent.getPigment();
    }

    @Override
    public @Nullable FrozenPigment setPigment(@Nullable FrozenPigment frozenPigment) {
        return null;
    }

    @Override
    public void produceParticles(ParticleSpray particleSpray, FrozenPigment frozenPigment) {
        this.parent.produceParticles(particleSpray,frozenPigment);
    }

    @Override
    public void printMessage(Text text) {
        this.parent.printMessage(text);
    }

    @Override
    public void postExecution(CastResult result) {
        for (var sideEffect : result.getSideEffects()) {
            if (sideEffect instanceof OperatorSideEffect.DoMishap doMishap) {
                var msg = doMishap.getMishap().errorMessageWithName(this, doMishap.getErrorCtx());
                if (msg != null) {
                    this.parent.printMessage(msg);
                }
                var list = new ArrayList<OperatorSideEffect>();
                list.add(doMishap);

                this.parent.postExecution(new CastResult(result.getCast(),
                        result.getContinuation(),
                        result.getNewData(),
                        list,
                        result.getResolutionType(),
                        result.getSound()));
                this.parent.circleState().endExecution(this.parent.getImpetus());
            }
        }
    }
}
