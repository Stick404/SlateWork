package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.sophia.slate_work.blocks.MacroLoci;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Stream;

@Mixin(BlockSlate.class)
public abstract class MixinBlockSlate {

    @Inject(method = "acceptControlFlow",
            at = @At(value = "INVOKE",
                    target = "Lat/petrak/hexcasting/api/casting/eval/vm/CastingVM;queueExecuteAndWrapIota(Lat/petrak/hexcasting/api/casting/iota/Iota;Lnet/minecraft/server/world/ServerWorld;)Lat/petrak/hexcasting/api/casting/eval/ExecutionClientView;"
            ), cancellable = true)
    void slate_works$acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs,
                                       ServerWorld world, CallbackInfoReturnable<ICircleComponent.ControlFlow> cir, @Local(name = "pattern") HexPattern pattern,
                                       @Local(name = "exitDirs") Stream exitDirs, @Local(name = "vm") CastingVM vm){
        var iotas = MacroLoci.getMacroIotas(imageIn, pattern, world);
        if (iotas != null) {
            var result = vm.queueExecuteAndWrapIotas(iotas, world);
            cir.setReturnValue(result.getResolutionType().getSuccess() ? new ICircleComponent.ControlFlow.Continue(vm.getImage(), exitDirs.toList()) : new ICircleComponent.ControlFlow.Stop());
            cir.cancel();
        }
    }
}
