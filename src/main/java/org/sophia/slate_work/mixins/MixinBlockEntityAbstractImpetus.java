package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.Slate_work;
import org.sophia.slate_work.misc.ICircleSpeedValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityAbstractImpetus.class)
public abstract class MixinBlockEntityAbstractImpetus extends HexBlockEntity implements SidedInventory {
    @Shadow
    @Nullable
    protected CircleExecutionState executionState;

    private MixinBlockEntityAbstractImpetus(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    @Inject(method = "saveModData(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)V",
    at = @At("HEAD"))
    private void Slate_works$arg_not_arg_for_saving(NbtCompound tag, RegistryWrapper.WrapperLookup registries, CallbackInfo ci){
        if (this.executionState != null) {
            ((ICircleSpeedValue) this.executionState).slate_work$setRegistries(registries);
        }
    }


    @WrapOperation(
            method = "saveModData",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/text/Text$Serialization;toJsonString(Lnet/minecraft/text/Text;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Ljava/lang/String;")
    )
    private String slate_works$cleaningUpOthersMessesSMH(Text text, RegistryWrapper.WrapperLookup registries, Operation<String> original){
        try {
            return original.call(text, registries);
        } catch (Exception e){
            Slate_work.LOGGER.severe("Slate Works just prevented a crash:\n" + e);
            Slate_work.LOGGER.severe("Attempted to serialize unstable text? Text was: " + text);
            return Text.Serialization.toJsonString(Text.translatable("slate_work.mixin.crash_prevent.text_serd"), registries);
        }
    }
}
