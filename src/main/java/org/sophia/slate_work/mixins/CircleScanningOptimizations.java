package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.misc.Result;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.SlateWorkConfig;
import org.sophia.slate_work.misc.ChunkScanning;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(CircleExecutionState.class)
public class CircleScanningOptimizations {

    @Unique
    private static final SlateWorkConfig configScanning = AutoConfig.getConfigHolder(SlateWorkConfig.class).getConfig();;

    @Inject(method = "createNew(Lat/petrak/hexcasting/api/casting/circles/BlockEntityAbstractImpetus;Lnet/minecraft/server/network/ServerPlayerEntity;)Lat/petrak/hexcasting/api/misc/Result;",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/ArrayList;<init>()V",
                    shift = At.Shift.AFTER, by = 1
            )
    )
    private static void slate_work$optimizeScanningInitChach(BlockEntityAbstractImpetus impetus, @Nullable ServerPlayerEntity caster, CallbackInfoReturnable<Result<CircleExecutionState, BlockPos>> cir, @Local(name = "level") ServerWorld level, @Share("slate_works$chunkCache") LocalRef<ChunkScanning> cache) {
        if (configScanning.aggressiveScanningOptimizations) {
            ChunkScanning scanner = new ChunkScanning(level);
            cache.set(scanner);
        }
    }

    @WrapOperation(method = "createNew(Lat/petrak/hexcasting/api/casting/circles/BlockEntityAbstractImpetus;Lnet/minecraft/server/network/ServerPlayerEntity;)Lat/petrak/hexcasting/api/misc/Result;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;")
    )
    private static BlockState slate_work$optimizeScanningInitChach(ServerWorld instance, BlockPos blockPos, Operation<BlockState> original, @Share("slate_works$chunkCache") LocalRef<ChunkScanning> cache){
        if (configScanning.aggressiveScanningOptimizations) {
            return cache.get().getBlock(blockPos);
        } else {
            return original.call(instance, blockPos);
        }
    }
}
