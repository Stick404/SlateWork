package org.sophia.slate_work.item;

import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtString;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.impetus.ListeningImpetusEntity;

import java.util.List;

public class WhisperingStone extends Item {
    public WhisperingStone(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var world = context.getWorld();
        if (!world.isClient()){
            var stack = context.getStack();
            var entity = world.getBlockEntity(context.getBlockPos());
            if (entity instanceof ListeningImpetusEntity listening && !listening.isDefault()){
                if (context.getPlayer() != null){
                    context.getPlayer().playSound(HexSounds.ABACUS, SoundCategory.PLAYERS, 1f, 1f);
                }
                stack.setSubNbt("cords", NbtHelper.fromBlockPos(listening.getPos()));
                stack.setSubNbt("string", NbtString.of(listening.getString()));
                return ActionResult.SUCCESS;
            }
            if (context.getPlayer() != null && context.getPlayer().isSneaking() && stack.getSubNbt("cords") != null) {
                context.getPlayer().playSound(HexSounds.ABACUS_SHAKE, SoundCategory.PLAYERS, 1f, 1f);
                stack.removeSubNbt("cords");
                stack.removeSubNbt("string");
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient){
            var stack = user.getStackInHand(hand);
            var cordNBT = stack.getSubNbt("cords");
            if (cordNBT != null && world.getBlockEntity(NbtHelper.toBlockPos(cordNBT)) instanceof ListeningImpetusEntity listening){
                var compound = new NbtCompound();
                compound.putString("stringed", listening.getString());
                stack.setSubNbt("string", compound);
            } else {
                stack.removeSubNbt("cords");
                stack.removeSubNbt("string");
            }
            return TypedActionResult.pass(stack);
        }
        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        var cordNBT = stack.getSubNbt("cords");
        var stringNBT = stack.getSubNbt("string");
        if (cordNBT != null && stringNBT != null){
            tooltip.add(Text.translatable("item.slate_work.whispering_stone.string").append(
                    Text.literal(stringNBT.getString("stringed")).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)
            ));
            tooltip.add(Text.translatable("item.slate_work.whispering_stone.cords").append(
                    Text.literal("(").append(NbtHelper.toBlockPos(cordNBT).toShortString()).append(")").formatted(Formatting.RED)
            ));
        } else {
            tooltip.add(Text.translatable("item.slate_work.whispering_stone.no_cords").formatted(Formatting.RED));
        }
    }
}
