package org.sophia.slate_work.blocks.entities;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.misc.KnownBroadcasters;
import org.sophia.slate_work.registries.BlockRegistry;

public class BroadcasterLociEntity extends BlockEntity {
    private NbtCompound iota;
    public BroadcasterLociEntity(BlockPos pos, BlockState state) {
        super(BlockRegistry.BROADCASTER_LOCI_ENTITY, pos, state);
        iota = IotaType.serialize(new NullIota());
    }

    public Iota getIota() {
        if (iota != null && world instanceof ServerWorld worldServer) {
            return IotaType.deserialize(iota, worldServer);
        }
        return new NullIota();
    }

    public NbtCompound getIotaCompound(){
        return iota;
    }

    public void setIota(Iota iota){
        KnownBroadcasters.INSTANCE.setBroadcaster(this, iota);
        this.iota = IotaType.serialize(iota);
        this.markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("iota", iota);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        iota = NBTHelper.getCompound(nbt, "iota");
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        var z = new NbtCompound();
        this.writeNbt(z);
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
