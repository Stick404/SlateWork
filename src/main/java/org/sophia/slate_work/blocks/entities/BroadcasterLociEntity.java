package org.sophia.slate_work.blocks.entities;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.utils.NBTHelper;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.misc.KnownBroadcasters;
import org.sophia.slate_work.registries.SlateWorksBlockRegistry;

public class BroadcasterLociEntity extends BlockEntity {
    private Iota iota;
    public BroadcasterLociEntity(BlockPos pos, BlockState state) {
        super(SlateWorksBlockRegistry.BROADCASTER_LOCI_ENTITY, pos, state);
        iota = new NullIota();
    }

    public Iota getIota() {
        if (iota != null && world instanceof ServerWorld worldServer) {
            return iota;
        }
        return new NullIota();
    }

    public void setIota(Iota iota){
        KnownBroadcasters.INSTANCE.setBroadcaster(this, iota);
        this.iota = iota;
        this.markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        Encoder<Iota> encoder = (Encoder<Iota>) iota.getType().codec().encoder();
        nbt.put("iota", encoder.encodeStart(NbtOps.INSTANCE, this.iota).getOrThrow());
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        Decoder<Iota> encoder = (Decoder<Iota>) iota.getType().codec().decoder();
        iota = encoder.decode(NbtOps.INSTANCE, NBTHelper.getCompound(nbt, "iota")).getOrThrow().getFirst();
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var z = new NbtCompound();
        this.writeNbt(z, registryLookup);
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
}
