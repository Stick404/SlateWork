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
    private NbtCompound save = (NbtCompound) CastingImage.getCODEC().encodeStart(NbtOps.INSTANCE, new CastingImage()).getOrThrow();

    public SaveLociEntity(BlockPos pos, BlockState state) {
        super(SlateWorksBlockRegistry.SAVE_LOCI_ENTITY, pos, state);
    }

    public void setSave(CastingImage image) {
        this.save = (NbtCompound) CastingImage.getCODEC().encodeStart(NbtOps.INSTANCE, image).getOrThrow();
        this.sync();
    }

    public NbtCompound getSave(){
        return this.save;
    }

    public CastingImage swapSave(CastingImage image){
        CastingImage output = CastingImage.getCODEC().decode(NbtOps.INSTANCE, this.save).getOrThrow().getFirst();

        this.setSave(image);
        return output;
    }

    @Override
    protected void saveModData(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        tag.put("save", save);
    }

    @Override
    protected void loadModData(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        save = tag.getCompound("save");
    }
}
