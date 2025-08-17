package org.sophia.slate_work.item;

import at.petrak.hexcasting.common.items.HexBaubleItem;
import at.petrak.hexcasting.common.lib.HexSounds;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
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
import org.sophia.slate_work.registries.AttributeRegistry;

import java.util.List;
import java.util.UUID;

public class WhisperingStone extends Item implements HexBaubleItem, Equipment {
    //TODO: Maybe make this wearable/use Attributes?
    public WhisperingStone(Settings settings) {
        super(settings);
    }

    public static final EntityAttributeModifier WHISPERING = new EntityAttributeModifier(
            UUID.fromString("8fe68dab-717e-4970-b0b3-be869fe608dd"),
            "Whispering Stone Speech", 1, EntityAttributeModifier.Operation.ADDITION);

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

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        var out = HashMultimap.create(super.getAttributeModifiers(slot));
        if (slot == EquipmentSlot.CHEST || slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            out.put(AttributeRegistry.WHISPERING, WHISPERING);
        }
        return out;
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getHexBaubleAttrs(ItemStack stack) {
        HashMultimap<EntityAttribute, EntityAttributeModifier> out = HashMultimap.create();
        out.put(AttributeRegistry.WHISPERING, WHISPERING);
        return out;
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.CHEST;
    }
}
