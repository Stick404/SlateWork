package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.sophia.slate_work.SlateWorkConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

@Mixin(CircleExecutionState.class)
public class CircleNBTOptimizations {
    public static String TAG_POSITIVE_POS = "positive_pos";
    public static String TAG_NEGATIVE_POS = "negative_pos";

    @Unique
    private static final SlateWorkConfig configScanning = AutoConfig.getConfigHolder(SlateWorkConfig.class).getConfig();

    @WrapOperation(
            method = "save()Lnet/minecraft/nbt/NbtCompound;",
            at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z")
    )
    private static boolean slate_work$begoneForLoop(Iterator<BlockPos> instance, Operation<Boolean> original){
        // If we are iterating over `knownPositions`, then kill the loop
        //if (configScanning.aggressiveNBTOptimizations && instance.getClass().getSimpleName().equals("KeyIterator")) {
        //    return false;
        //}
        return original.call(instance);
    }

    @Inject(
            method = "save()Lnet/minecraft/nbt/NbtCompound;",
            at = @At(value = "RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void slate_work$myOwnSickSave(CallbackInfoReturnable<NbtCompound> cir){

    }
}
