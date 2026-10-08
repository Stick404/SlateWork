package org.sophia.slate_work.blocks.entities;

import at.petrak.hexcasting.api.utils.NBTHelper;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.ListCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static org.sophia.slate_work.registries.SlateWorksBlockRegistry.TRADE_LOCI_ENTITY;

public class TradeLociEntity extends BlockEntity {
    public TradeOfferList offerList;
    public VillagerData villagerData;
    public int xp;
    public long lastRestockTime;
    private final Random random;

    public TradeLociEntity(BlockPos pos, BlockState state) {
        super(TRADE_LOCI_ENTITY, pos, state);
        offerList = new TradeOfferList();
        villagerData = new VillagerData(VillagerType.PLAINS, VillagerProfession.NITWIT, 1);
        this.xp = 0;
        this.lastRestockTime = 0;
        this.random = Random.create();
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        var villagerData = VillagerData.CODEC.decode(NbtOps.INSTANCE, nbt.get("data"));
        if (villagerData.isSuccess()) {
            this.villagerData = villagerData.getOrThrow().getFirst();
        } else {
            this.villagerData = new VillagerData(VillagerType.PLAINS, VillagerProfession.NITWIT, 1);
        }

        var offerList = TradeOffer.CODEC.listOf().decode(registries.getOps(NbtOps.INSTANCE), nbt.getList("offers", NbtElement.COMPOUND_TYPE));
        if (offerList.isSuccess()) {
            this.offerList = new TradeOfferList();
            this.offerList.addAll(offerList.getOrThrow().getFirst());
        } else {
            this.offerList = new TradeOfferList();
        }

        this.xp = nbt.getInt("xp");
        this.lastRestockTime = nbt.getLong("LastRestockTime");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        var data = VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, villagerData);
        if (data.isSuccess()) {
            nbt.put("data", data.getOrThrow());
        }

        var offers = TradeOffer.CODEC.listOf().encodeStart(registries.getOps(NbtOps.INSTANCE), this.offerList);
        if (offers.isSuccess()) {
            nbt.put("offers", offers.getOrThrow());
        }
        nbt.putInt("xp", this.xp);
        nbt.putLong("LastRestockTime", this.lastRestockTime);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        var z = new NbtCompound();
        this.writeNbt(z, registries);
        return z;
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }

    public void slurpVillager(VillagerEntity entity){
        TradeOfferList tempOfferList = (TradeOfferList) entity.getOffers().clone();
        VillagerData tempVillagerData = entity.getVillagerData();
        int tempXp = entity.getExperience();

        entity.setVillagerData(this.villagerData);
        entity.setOffers((TradeOfferList) this.offerList.clone());
        entity.setExperience(this.xp);

        this.offerList = tempOfferList;
        this.villagerData = tempVillagerData;
        this.xp = tempXp;
        this.markDirty();
    }

    public void levelUpCheck(){
        int level = this.villagerData.getLevel();
        if (VillagerData.canLevelUp(level) && this.xp >= VillagerData.getUpperLevelExperience(level)) {
            this.villagerData = new VillagerData(this.villagerData.getType(), this.villagerData.getProfession(), this.villagerData.getLevel() + 1);
            VillagerData villagerData = this.villagerData;
            Int2ObjectMap<TradeOffers.Factory[]> int2ObjectMap = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(villagerData.getProfession());
            if (int2ObjectMap != null && !int2ObjectMap.isEmpty()) {
                TradeOffers.Factory[] factorys = int2ObjectMap.get(villagerData.getLevel());
                if (factorys != null) {
                    TradeOfferList tradeOfferList = this.offerList;
                    this.fillRecipesFromPool(tradeOfferList, factorys, 2);
                }
            }
            this.markDirty();
        }
    }

    protected void fillRecipesFromPool(TradeOfferList recipeList, TradeOffers.Factory[] pool, int count) {
        Set<Integer> set = Sets.newHashSet();
        if (pool.length > count) {
            while(set.size() < count) {
                set.add(this.random.nextInt(pool.length));
            }
        } else {
            for(int i = 0; i < pool.length; ++i) {
                set.add(i);
            }
        }

        for(Integer integer : set) {
            TradeOffers.Factory factory = pool[integer];
            // How... bad could this be
            TradeOffer tradeOffer = factory.create(null, this.random);
            if (tradeOffer != null) {
                recipeList.add(tradeOffer);
            }
        }

    }

    public static void tick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof TradeLociEntity entity && world instanceof ServerWorld) {
            if ( world.getTimeOfDay() % 13000 == 0 ||
                    (entity.lastRestockTime + 13000) <= world.getTime()) {
                entity.offerList.forEach(a -> {
                    a.updateDemandBonus();
                    a.resetUses();
                    entity.lastRestockTime = entity.world.getTime();
                    entity.markDirty();
                });
            }
        }
    }
}
