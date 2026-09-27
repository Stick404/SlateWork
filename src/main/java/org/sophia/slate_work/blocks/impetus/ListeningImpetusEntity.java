package org.sophia.slate_work.blocks.impetus;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.common.lib.HexSounds;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.misc.ICircleSpeedValue;
import org.sophia.slate_work.mixins.MixinCircleExecInvoker;
import org.sophia.slate_work.registries.SlateWorksBlockRegistry;
import ram.talia.moreiotas.api.casting.iota.StringIota;

import java.util.List;

public class ListeningImpetusEntity extends BlockEntityAbstractImpetus implements ADIotaHolder {
    public static final String DEFAULT =
            "Hi! You just found out about the default Listening Impetus string. If you send this exact one, it will be triggered. This was a bug, but I kept it because it was funny";
    private String string = DEFAULT;
    private EntityIota playerIota = null;
    private StringIota textIota = null;

    public ListeningImpetusEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(SlateWorksBlockRegistry.LISTENING_IMPETUS_ENTITY, pWorldPosition, pBlockState);
    }

    public void setIotas(EntityIota player, StringIota text){
        this.textIota = text;
        this.playerIota = player;
    }

    public boolean isRunning(){
        return this.executionState != null;
    }

    @Override
    public void startExecution(ServerPlayerEntity player) {
        var realPlayer = player;
        if (this.world == null)
            return; // TODO: error here?
        if (this.world.isClient)
            return; // TODO: error here?

        if (playerIota == null && textIota == null)
            return;

        if (this.executionState != null) {
            return;
        }
        var result = CircleExecutionState.createNew(this, null);
        if (result.isErr()) {
            var errPos = result.unwrapErr();
            if (errPos == null) {
                ICircleComponent.sfx(this.getPos(), this.getCachedState(), this.world, null, false);
                this.postNoExits(this.getPos());
            } else {
                ICircleComponent.sfx(errPos, this.world.getBlockState(errPos), this.world, null, false);
                this.postDisplay(Text.translatable("hexcasting.tooltip.circle.no_closure",
                                Text.literal(errPos.toShortString()).formatted(Formatting.RED)),
                        new ItemStack(Items.LEAD));
            }

            return;
        }
        realPlayer.playSound(HexSounds.IMPETUS_REDSTONE_DING.value(), 1f, 0.5f);
        this.executionState = result.unwrap();
        var image = this.executionState.currentImage;
        var stack = TreeList.<Iota>empty();
        stack = stack.appended(textIota);
        stack = stack.appended(playerIota);

        var newImage = image.copy(stack, image.getParenCount(), image.getParenthesized(), image.getEscapeNext(), image.getSimulateNext(), image.getOpsConsumed(), image.getUserData());
        ((ICircleSpeedValue) this.executionState).slate_work$setImage(newImage);

        this.clearDisplay();
        var serverLevel = (ServerWorld) this.world;
        serverLevel.scheduleBlockTick(this.getPos(), this.getCachedState().getBlock(),
                ((MixinCircleExecInvoker) this.executionState).slate_work$getTickSpeed());
        serverLevel.setBlockState(this.getPos(),
                this.getCachedState().with(BlockCircleComponent.ENERGIZED, true));
    }

    public String getString() {
        return string;
    }

    public boolean setString(String string) {
        if (!string.isBlank()){
            this.string = string;
            return true;
        }
        return false;
    }

    public boolean isDefault(){
        return DEFAULT.equals(string);
    }

    @Override
    public void clear() {
        this.setString(DEFAULT);
    }

    @Override
    public void applyScryingLensOverlay(List<Pair<ItemStack, Text>> lines, BlockState state, BlockPos pos,
                                        PlayerEntity observer, World world, Direction hitFace) {
        super.applyScryingLensOverlay(lines, state, pos, observer, world, hitFace);
        if (this.isDefault()){
            lines.add(new Pair<>(Items.NAME_TAG.getDefaultStack(), Text.translatable("slate_work.scrying.impetus.listening.unbound")));
        } else {
            lines.add(new Pair<>(Items.NAME_TAG.getDefaultStack(),
                    Text.translatable("slate_work.scrying.impetus.listening.bound").append(Text.literal(" " + this.string).formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))));
        }
    }

    @Override
    protected void saveModData(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        super.saveModData(tag, registries);
        tag.putString("string_listen", string);
    }

    @Override
    protected void loadModData(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        super.loadModData(tag, registries);
        this.setString(tag.getString("string_listen"));
    }

    @Override
    public @Nullable Iota readIota() {
        return StringIota.makeUnchecked(this.string);
    }

    @Override
    public boolean writeIota(@Nullable Iota iota, boolean simulate) {
        if (iota instanceof StringIota text){
            if (!text.getString().isBlank()){
                if (!simulate) {
                    this.string = text.getString();
                    this.sync();
                }
                return true;
            }
        } else if (iota instanceof NullIota) {
            if (!simulate){
                this.string = ListeningImpetusEntity.DEFAULT;
                this.sync();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean writeable() {
        return true;
    }
}
