package org.sophia.slate_work.compat;

import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.sophia.slate_work.misc.ChatHelper;
import org.sophia.slate_work.registries.BlockRegistry;
import org.sophia.slate_work.storage.SlateFakePlayer;

import java.util.UUID;

public class SlateWorksTrinkets {
    public static void init(){
        TrinketsApi.registerTrinket(BlockRegistry.WHISPERING_STONE, new Trinket() {
            @Override
            public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot,
                                                                       LivingEntity entity, UUID uuid) {
                var map = Trinket.super.getModifiers(stack, slot, entity, uuid);
                map.putAll(BlockRegistry.WHISPERING_STONE.getHexBaubleAttrs(stack));
                return map;
            }
        });
    }

    public static void finder(ChatHelper.ItemHold hold, ServerPlayerEntity sender){
        var opt = TrinketsApi.getTrinketComponent(sender);
        if (opt.isEmpty()) return;

        for (var z : opt.get().getEquipped(BlockRegistry.WHISPERING_STONE)){
            hold.setStack(z.getRight());
            break;
        }
    }

    public static void makeFakeDropTrinkets(SlateFakePlayer fakePlayer) {
        var check = TrinketsApi.getTrinketComponent(fakePlayer);
        if (check.isPresent()) {
            for (var slot : check.get().getAllEquipped()){
                fakePlayer.dropItem(slot.getRight(), true, false);
            }
        }
    }
}