package org.sophia.slate_work.blocks.entities;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.sophia.slate_work.registries.SlateWorksBlockRegistry;

public class SaveLociEntity extends HexBlockEntity {
    private CastingImage save = new CastingImage();

    public SaveLociEntity(BlockPos pos, BlockState state) {
        super(SlateWorksBlockRegistry.SAVE_LOCI_ENTITY, pos, state);
    }

    public void setSave(CastingImage image) {
        this.save = image; //(NbtCompound) CastingImage.getCODEC().encodeStart(NbtOps.INSTANCE, image).getOrThrow();
        this.sync();
    }

    public CastingImage getSave(){
        return this.save;
    }

    public CastingImage swapSave(CastingImage image){
        CastingImage output = this.getSave();

        this.setSave(image);
        return output;
    }

    @Override
    protected void saveModData(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        var image = CastingImage.getCODEC().encodeStart(NbtOps.INSTANCE, this.save);
        if (image.isSuccess()) {
            tag.put("save", image.getOrThrow());
        }
    }

    @Override
    protected void loadModData(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        var image = CastingImage.getCODEC().decode(NbtOps.INSTANCE, tag.getCompound("save"));
        if (image.isSuccess()) {
            this.save = image.getOrThrow().getFirst();
        }
    }
}
