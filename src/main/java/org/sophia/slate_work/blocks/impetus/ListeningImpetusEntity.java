package org.sophia.slate_work.blocks.impetus;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import com.mojang.datafixers.util.Pair;
import miyucomics.hexpose.iotas.TextIota;
import miyucomics.hexpose.utils.ChatHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.Slate_work;
import org.sophia.slate_work.misc.ICircleSpeedValue;
import org.sophia.slate_work.mixins.MixinCircleExecInvoker;
import org.sophia.slate_work.registries.BlockRegistry;

import java.util.ArrayList;
import java.util.List;

public class ListeningImpetusEntity extends BlockEntityAbstractImpetus {
    public static final String DEFAULT = "You know, people dont need to know that this is the default string for these. Like, I just need to check if this matches this to see if its blank";
    private String string = DEFAULT;
    private EntityIota playerIota = null;
    private TextIota textIota = null;

    public ListeningImpetusEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockRegistry.LISTENING_IMPETUS_ENTITY, pWorldPosition, pBlockState);
    }

    public void setIotas(EntityIota player, TextIota text){
        this.textIota = text;
        this.playerIota = player;
    }

    @Override
    public void startExecution(@Nullable ServerPlayerEntity player) {

        if (this.world == null)
            return; // TODO: error here?
        if (player.getWorld().isClient())
            return; // TODO: error here?
        this.world = player.getWorld(); // Hate.
        if (playerIota == null && textIota == null)
            return;

        if (this.executionState != null) {
            return;
        }
        var result = CircleExecutionState.createNew(this, player);
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
        this.executionState = result.unwrap();
        var image = this.executionState.currentImage;
        var stack = new ArrayList<Iota>();
        stack.add(textIota);
        stack.add(playerIota);
        var newImage = image.copy(stack, image.getParenCount(), image.getParenthesized(), image.getEscapeNext(), image.getOpsConsumed(), image.getUserData());
        ((ICircleSpeedValue) this.executionState).Slate_work$setImage(newImage);

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

    public boolean IsDefault(){
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
        if (this.IsDefault()){
            lines.add(new Pair<>(Items.NAME_TAG.getDefaultStack(), Text.translatable("slate_work.scrying.impetus.listening.unbound")));
        } else {
            lines.add(new Pair<>(Items.NAME_TAG.getDefaultStack(),
                    Text.translatable("slate_work.scrying.impetus.listening.bound").append(Text.literal(" " + this.string).formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))));
        }
    }

    @Override
    protected void saveModData(NbtCompound tag) {
        super.saveModData(tag);
        tag.putString("string_listen", string);
        if (world != null && !world.isClient()){
            Slate_work.LISTENERS.put(this.pos, this);
        }
    }

    @Override
    protected void loadModData(NbtCompound tag) {
        super.loadModData(tag);
        this.setString(tag.getString("string_listen"));
        if (world != null && !world.isClient()){
             Slate_work.LISTENERS.put(this.pos, this);
        }
    }
}
