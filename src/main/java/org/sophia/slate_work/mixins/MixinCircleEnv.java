package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound;
import at.petrak.hexcasting.api.utils.MediaHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.entities.HotbarLociEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.print.attribute.standard.Media;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(CircleCastEnv.class)
public abstract class MixinCircleEnv extends CastingEnvironment{
    public MixinCircleEnv(ServerWorld world, CircleExecutionState execState) {
        super(world);
    }

    @Shadow public abstract CircleExecutionState circleState();

    @Shadow @Final protected CircleExecutionState execState;

    @Shadow public abstract @Nullable BlockEntityAbstractImpetus getImpetus();

    @WrapOperation(method = "postExecution(Lat/petrak/hexcasting/api/casting/eval/CastResult;)V",
            at = @At(value = "INVOKE",
                    target = "Lat/petrak/hexcasting/api/casting/eval/sideeffects/EvalSound;sound()Lnet/minecraft/sound/SoundEvent;"
            )
    )
    public SoundEvent slate_work$shushYouToo(EvalSound instance, Operation<SoundEvent> original){
        var image = this.circleState().currentImage;
        var volume = image.getUserData().getFloat("volume");
        var mute = image.getUserData().getBoolean("mute");
        if (mute && this.getImpetus() != null && volume != 0 && instance.sound() != null){
            if (this.getImpetus().getWorld() != null) {
                BlockPos soundPos = this.execState.currentPos;
                this.getImpetus().getWorld().playSound(null, soundPos, instance.sound(), SoundCategory.PLAYERS, volume, 1.0F);
            }
        }
        return null;
    }

    @Inject(method = "getUsableStacks", at = @At("RETURN"), cancellable = true, remap = false)
    private void slate_work$getUsableStacks(CastingEnvironment.StackDiscoveryMode mode, CallbackInfoReturnable<List<ItemStack>> cir){
        var data = this.execState.currentImage.getUserData();
        if (world.getBlockEntity(NbtHelper.toBlockPos(data.getCompound("hotbar_loci"))) instanceof HotbarLociEntity entity){
            var list = cir.getReturnValue();
            list.addAll(entity.getStacksSorted());
            cir.setReturnValue(list);
        }
    }

    @Inject(method = "getPrimaryStacks", at = @At("RETURN"), cancellable = true, remap = false)
    private void slate_work$gePrimaryStacks(CallbackInfoReturnable<List<HeldItemInfo>> cir){
        var data = this.execState.currentImage.getUserData();
        if (world.getBlockEntity(NbtHelper.toBlockPos(data.getCompound("hotbar_loci"))) instanceof HotbarLociEntity entity){
            var list = new ArrayList<>(cir.getReturnValue()); //makes it Mutable
            list.add(new HeldItemInfo(entity.getCurrentSlot(), Hand.OFF_HAND));
            cir.setReturnValue(list);
        }
    }

    @Inject(method = "replaceItem", at = @At("RETURN"), cancellable = true)
    private void slate_work$replaceItem(Predicate<ItemStack> stackOk, ItemStack replaceWith, @Nullable Hand hand, CallbackInfoReturnable<Boolean> cir){
        if (cir.getReturnValue()) return;
        var data = this.execState.currentImage.getUserData();
        if (world.getBlockEntity(NbtHelper.toBlockPos(data.getCompound("hotbar_loci"))) instanceof HotbarLociEntity entity){
            int slot = 0;
            for (ItemStack stack: entity.getStacksSorted()){
                if (stackOk.test(stack)){
                    entity.setStack(slot, replaceWith);
                    entity.sync();
                    cir.setReturnValue(true);
                    return;
                }
                slot++;
            }
        }
    }

    @Inject(method = "extractMediaEnvironment", at = @At("RETURN"), cancellable = true, remap = false)
    private void slate_work$extractMedia(long cost, boolean simulate, CallbackInfoReturnable<Long> cir){
        var data = this.execState.currentImage.getUserData();
        if (world.getBlockEntity(NbtHelper.toBlockPos(data.getCompound("hotbar_loci"))) instanceof HotbarLociEntity entity){
            var media = cir.getReturnValue();

            ArrayList<ADMediaHolder> sources = new ArrayList<>();
            for (ItemStack item : entity.getStacksSorted()) {
                var holder = HexAPI.instance().findMediaHolder(item);
                if (holder != null && holder.canProvide()) sources.add(holder);
            }
            sources.sort(MediaHelper::compareMediaItem);

            for (var source : sources) {
                var found = MediaHelper.extractMedia(source, media, false, simulate);
                media -= found;
                if (media <= 0) {
                    break;
                }
            }
            cir.setReturnValue(media);
        }
    }
}
