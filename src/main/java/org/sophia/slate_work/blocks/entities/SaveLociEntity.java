package org.sophia.slate_work.blocks.entities;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.sophia.slate_work.registries.BlockRegistry;

public class SaveLociEntity extends HexBlockEntity {
    private NbtCompound save = new CastingImage().serializeToNbt();

    public SaveLociEntity(BlockPos pos, BlockState state) {
        super(BlockRegistry.SAVE_LOCI_ENTITY, pos, state);
    }

    public void setSave(CastingImage image) {
        this.save = image.serializeToNbt();
        this.sync();
    }

    public NbtCompound getSave(){
        return this.save;
    }

    public CastingImage swapSave(CastingImage image, ServerWorld world){
        CastingImage output = CastingImage.loadFromNbt(this.getSave(), world);
        this.setSave(image);
        return output;
    }

    @Override
    protected void saveModData(NbtCompound tag) {
        tag.put("save", save);
    }

    @Override
    protected void loadModData(NbtCompound tag) {
        save = tag.getCompound("save");
    }
}
