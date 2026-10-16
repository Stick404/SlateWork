package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.sophia.slate_work.Slate_work;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.logging.Level;

@Mixin(BlockEntityAbstractImpetus.class)
public abstract class MixinBlockEntityAbstractImpetus extends HexBlockEntity implements SidedInventory {
    private MixinBlockEntityAbstractImpetus(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
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
