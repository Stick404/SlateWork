package org.sophia.slate_work.storage;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.sophia.slate_work.blocks.entities.HotbarLociEntity;

// Implementation inspired by Create
// https://github.com/Fabricators-of-Create/Create/blob/mc1.20.1/fabric/dev/src/main/java/com/simibubi/create/content/kinetics/deployer/DeployerFakePlayer.java#L45
public class SlateFakePlayer extends FakePlayer {
    SlateFakePlayerInv inventory;
    public SlateFakePlayer(ServerWorld world, HotbarLociEntity entity) {
        super(world, new GameProfile(DEFAULT_UUID, "[SlateFakePlayer]"));
        inventory = new SlateFakePlayerInv(this, entity);
    }

    @Override
    public ItemStack getStackInHand(Hand hand) {
        return inventory.getHotbarLociEntity().getCurrentSlot();
    }

    @Override
    public void setStackInHand(Hand hand, ItemStack stack) {
        inventory.setStack(0, stack);
    }

    @Override
    public ItemStack getActiveItem() {
        return inventory.getHotbarLociEntity().getCurrentSlot();
    }

    @Override
    public SlateFakePlayerInv getInventory() {
        return inventory;
    }

    // TODO: Mixin!
    @Override
    public ItemEntity dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        return super.dropItem(stack, throwRandomly, retainOwnership);
    }

    @Override
    public float getAttackCooldownProgressPerTick() {
        return 1 / 64f;
    }

    @Override
    public ItemStack eatFood(World world, ItemStack stack) {
        stack.decrement(1);
        return stack;
    }

    @Override
    public boolean canConsume(boolean ignoreHunger) {
        return false;
    }

    @Override
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        return false;
    }
}
