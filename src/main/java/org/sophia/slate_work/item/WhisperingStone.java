package org.sophia.slate_work.item;

import at.petrak.hexcasting.common.items.HexBaubleItem;
import at.petrak.hexcasting.common.lib.HexSounds;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.sophia.slate_work.blocks.impetus.ListeningImpetusEntity;
import org.sophia.slate_work.registries.SlateWorksAttributeRegistry;
import org.sophia.slate_work.registries.SlateWorksComponents;

import java.util.List;

import static org.sophia.slate_work.Slate_work.MOD_ID;

public class WhisperingStone extends Item implements HexBaubleItem, Equipment {
    public WhisperingStone(Settings settings) {
        var builder = AttributeModifiersComponent.builder().add(SlateWorksAttributeRegistry.WHISPERING, WHISPERING_HELD,
                AttributeModifierSlot.ANY);

        settings = settings.attributeModifiers(builder.build());
        super(settings);

    }
    public static final EntityAttributeModifier WHISPERING_HELD = new EntityAttributeModifier(
            Identifier.of(MOD_ID, "whispering_held"),
            1, EntityAttributeModifier.Operation.ADD_VALUE);

    public static final EntityAttributeModifier WHISPERING_WORN = new EntityAttributeModifier(
            Identifier.of(MOD_ID, "whispering_worn"),
            1, EntityAttributeModifier.Operation.ADD_VALUE);

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var world = context.getWorld();
        if (!world.isClient()){
            var stack = context.getStack();
            var entity = world.getBlockEntity(context.getBlockPos());
            if (entity instanceof ListeningImpetusEntity listening && !listening.isDefault()){
                if (context.getPlayer() != null){
                    context.getPlayer().playSound(HexSounds.ABACUS.value(), 1f, 1f);
                }
                stack.set(SlateWorksComponents.WHISPERING_STONE_COMPONENT, new SlateWorksComponents.WhisperingStoneComponent(
                        listening.getString(),
                        listening.getPos(),
                        listening.getWorld().getRegistryKey().getValue()
                ));
                return ActionResult.SUCCESS;
            }
            if (context.getPlayer() != null && context.getPlayer().isSneaking() && stack.getComponents().contains(SlateWorksComponents.WHISPERING_STONE_COMPONENT)) {
                context.getPlayer().playSound(HexSounds.ABACUS_SHAKE.value(), 1f, 1f);
                stack.remove(SlateWorksComponents.WHISPERING_STONE_COMPONENT);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient){
            var stack = user.getStackInHand(hand);
            SlateWorksComponents.WhisperingStoneComponent component =
                    stack.getComponents().getOrDefault(SlateWorksComponents.WHISPERING_STONE_COMPONENT, null);

            if (component != null && world.getBlockEntity(component.pos()) instanceof ListeningImpetusEntity listening){
                stack.set(SlateWorksComponents.WHISPERING_STONE_COMPONENT, new SlateWorksComponents.WhisperingStoneComponent(
                        listening.getString(),
                        listening.getPos(),
                        listening.getWorld().getRegistryKey().getValue()
                ));

            } else {
                stack.remove(SlateWorksComponents.WHISPERING_STONE_COMPONENT);
            }
            return TypedActionResult.pass(stack);
        }
        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        SlateWorksComponents.WhisperingStoneComponent component = stack.getOrDefault(SlateWorksComponents.WHISPERING_STONE_COMPONENT, null);

        if (component != null){
            tooltip.add(Text.translatable("item.slate_work.whispering_stone.string").append(
                    Text.literal(component.string()).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)
            ));
            tooltip.add(Text.translatable("item.slate_work.whispering_stone.cords").append(
                    Text.literal("(").append(component.pos().toShortString()).append(")").formatted(Formatting.RED)
            ));
        } else {
            tooltip.add(Text.translatable("item.slate_work.whispering_stone.no_cords").formatted(Formatting.RED));
        }
    }

    @Override
    public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getHexBaubleAttrs(ItemStack stack) {
        Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> out = HashMultimap.create();
        out.put(SlateWorksAttributeRegistry.WHISPERING, WHISPERING_WORN);
        return out;
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.CHEST;
    }
}
