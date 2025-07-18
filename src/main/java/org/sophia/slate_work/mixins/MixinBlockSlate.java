package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.SpellList;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.ExecutionClientView;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
            ), cancellable = true, remap = false)
    void circleMacros(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs,
                      ServerWorld world, CallbackInfoReturnable<ICircleComponent.ControlFlow> cir, @Local HexPattern pattern,
                      @Local Stream exitDirs, @Local CastingVM vm){
        var macroNBT = imageIn.getUserData().getList("macros", NbtElement.COMPOUND_TYPE);
        String angleSig = pattern.anglesSignature();

        Map<String, SpellList> macros = new HashMap<>();
        for (NbtElement temp : macroNBT) {
            NbtCompound nbtElement = (NbtCompound) temp;
            macros.put(
                    HexPattern.fromNBT(nbtElement.getCompound("pattern")).anglesSignature(),
                    ((ListIota) IotaType.deserialize(nbtElement.getCompound("macro") ,world)).getList()
            );

        }
        if (macros.containsKey(angleSig)){
            List<Iota> realSpell = new ArrayList<>();
            for (Iota iota : macros.get(angleSig)){
                realSpell.add(iota);
            }

            ExecutionClientView result = vm.queueExecuteAndWrapIotas(realSpell, world);
            cir.setReturnValue(result.getResolutionType().getSuccess() ? new ICircleComponent.ControlFlow.Continue(vm.getImage(), exitDirs.toList()) : new ICircleComponent.ControlFlow.Stop());
            cir.cancel();
        }
    }
}
