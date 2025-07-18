package org.sophia.slate_work.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.sophia.slate_work.registries.BlockRegistry;

import java.util.Optional;

import static at.petrak.hexcasting.api.block.circle.BlockCircleComponent.ENERGIZED;
import static org.sophia.slate_work.Slate_work.MOD_ID;
import static org.sophia.slate_work.blocks.AbstractSlate.FACING;

public class BlockModelDatagen extends FabricModelProvider {
    public BlockModelDatagen(FabricDataOutput output) {
        super(output);
    }

    public static final Model ERNERGIZED = new Model(
            Optional.empty(),
            Optional.of("energized"),
            TextureKey.TEXTURE
    );

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        var bsvEner = BlockStateVariant.create().put(VariantSettings.MODEL, new Identifier(MOD_ID,"storage_loci_energized"));
        var bsvNorm = BlockStateVariant.create().put(VariantSettings.MODEL, new Identifier(MOD_ID,"storage_loci"));

        var bsvRot1 = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R0);
        var bsvRot2 = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R90);
        var bsvRot3 = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R180);
        var bsvRot4 = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R270);

        var bsvRot5 = BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R0);
        var bsvRot6 = BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R90);
        var bsvRot7 = BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R180);
        var bsvRot8 = BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R270);

        BlockStateVariantMap mapEner = BlockStateVariantMap.create(ENERGIZED)
                .register(false,bsvEner)
                .register(true,bsvNorm);
        BlockStateVariantMap mapFac = BlockStateVariantMap.create(FACING)
                .register(Direction.DOWN,bsvRot1)
                .register(Direction.SOUTH,bsvRot2)
                .register(Direction.UP,bsvRot3)
                .register(Direction.NORTH,bsvRot4)
                .register(Direction.EAST,bsvRot6)
                .register(Direction.WEST,bsvRot7);


        blockStateModelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(BlockRegistry.STORAGE_LOCI).coordinate(mapEner).coordinate(mapFac));
        blockStateModelGenerator.registerParentedItemModel(BlockRegistry.STORAGE_LOCI,new Identifier(MOD_ID,"ambit_loci"));
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {

    }
}
