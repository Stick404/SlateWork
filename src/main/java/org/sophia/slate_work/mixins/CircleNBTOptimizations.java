package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.sophia.slate_work.SlateWorkConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

import static at.petrak.hexcasting.api.casting.circles.CircleExecutionState.TAG_KNOWN_POSITIONS;

@Mixin(CircleExecutionState.class)
public abstract class CircleNBTOptimizations {
    @Shadow
    @Final
    public Box bounds;
    @Unique
    private static final String TAG_POSITIVE_POS = "positive_pos";
    @Unique
    private static final String TAG_NEGATIVE_POS = "negative_pos";

    @Unique
    private static final SlateWorkConfig configScanning = AutoConfig.getConfigHolder(SlateWorkConfig.class).getConfig();

    @WrapOperation(
            method = "save()Lnet/minecraft/nbt/NbtCompound;",
            at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z")
    )
    private static boolean slate_work$begoneForLoop(Iterator<BlockPos> instance, Operation<Boolean> original){
        // If we are iterating over `knownPositions`, then kill the loop
        if (configScanning.aggressiveNBTOptimizations && instance.getClass().getSimpleName().equals("KeyIterator")) {
            return false;
        }
        return original.call(instance);
    }

    @Inject(
            method = "save()Lnet/minecraft/nbt/NbtCompound;",
            at = @At(value = "RETURN"),
            cancellable = true)
    private void slate_work$myOwnSickSave(CallbackInfoReturnable<NbtCompound> cir){
        if (configScanning.aggressiveNBTOptimizations){
            NbtCompound compound = cir.getReturnValue();
            compound.remove(TAG_KNOWN_POSITIONS);

            compound.put(TAG_POSITIVE_POS,
                    NbtHelper.fromBlockPos(new BlockPos((int) this.bounds.maxX, (int) this.bounds.maxY, (int) this.bounds.maxZ)));
            compound.put(TAG_NEGATIVE_POS,
                    NbtHelper.fromBlockPos(new BlockPos((int) this.bounds.minX, (int) this.bounds.minY, (int) this.bounds.minZ)));

            cir.setReturnValue(compound);
        }
    }

    @Inject(
            method = "load",
            at = @At(value = "RETURN"),
            cancellable = true
    )
    private static void slate_work$myOwnStickLoad(NbtCompound nbt, ServerWorld world, CallbackInfoReturnable<CircleExecutionState> cir,
                                                 @Local(name = "startPos") BlockPos startPos, @Local(name = "startDir") Direction startDir,
                                                 @Local(name = "reachedPositions") ArrayList<BlockPos> reachedPositions,
                                                 @Local(name = "currentPos") BlockPos currentPos, @Local(name = "enteredFrom") Direction enteredFrom,
                                                 @Local(name = "image") CastingImage image, @Local(name = "caster") UUID caster,
                                                 @Local(name = "pigment") FrozenPigment pigment){
        if (configScanning.aggressiveNBTOptimizations) {
            BlockPos farTop = NbtHelper.toBlockPos(nbt.getCompound(TAG_POSITIVE_POS));
            BlockPos farBottom = NbtHelper.toBlockPos(nbt.getCompound(TAG_NEGATIVE_POS));

            Set<BlockPos> trickedYou = new HashSet<>();
            trickedYou.add(farTop);
            trickedYou.add(farBottom);

            CircleExecutionState state = INewCustomCircleExec.newState(startPos, startDir, trickedYou, reachedPositions,
                    currentPos, enteredFrom, image, caster, pigment);
            cir.setReturnValue(state);
        }
    }
}
