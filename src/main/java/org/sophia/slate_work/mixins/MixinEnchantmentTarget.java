package org.sophia.slate_work.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.sophia.slate_work.item.BlockBreakLociItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// "Oh its just a quick mixin"
// "Oh it will just take a little bit to set up"
// "You know, this is exactly what Ae2 does! I'll follow that"
// I have spent over an hour and 30 mins on this one mixin due to Anon Classes and enums

// 1: Armor
// 2: Breakable
// 3: Bow
// 4: Wearable
// 5: Crossbow
// 6: Vanishable
// 7: FEET
// 8: Leggings
// 9: Chestplate
// 10: Helmet
// 11: Weapon
// 12: DIGGER FINALLY
@Mixin(targets = "net.minecraft.enchantment.EnchantmentTarget$12")
public abstract class MixinEnchantmentTarget {

    @ModifyReturnValue(
            method = "isAcceptableItem",
            at = @At("RETURN")

    )
    private boolean slate_work$IsAMixinReallyTheBestWayToMakeAnItemEnchantableAsAPickaxeQuestionMark(boolean original, Item item){
        return item instanceof BlockBreakLociItem;
    }
}
