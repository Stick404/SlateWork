package org.sophia.slate_work.blocks.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.registries.BlockRegistry;

public class SentinelLociEntity extends BlockEntity {
    private Vec3d pos = this.getPos().toCenterPos();

    public SentinelLociEntity(BlockPos pos, BlockState state) {
        super(BlockRegistry.SENTINEL_LOCI_ENTITY, pos, state);
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

    // Need to make name it this or else it tries to Override the method in BlockEntity
    public Vec3d getSentPos() {
        return pos;
    }

    public void setSentPos(Vec3d pos) {
        this.pos = pos;
        this.markDirty();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.pos = new Vec3d(
                nbt.getDouble("xSent"),
                nbt.getDouble("ySent"),
                nbt.getDouble("zSent")
        );
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putDouble("xSent", this.pos.x);
        nbt.putDouble("ySent", this.pos.y);
        nbt.putDouble("zSent", this.pos.z);
        super.writeNbt(nbt);
    }
}
