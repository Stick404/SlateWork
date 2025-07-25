package org.sophia.slate_work.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.sophia.slate_work.registries.BlockRegistry;

import static at.petrak.hexcasting.api.block.circle.BlockCircleComponent.ENERGIZED;
import static org.sophia.slate_work.Slate_work.MOD_ID;
import static org.sophia.slate_work.blocks.AbstractSlate.FACING;

public class BlockModelDatagen extends FabricModelProvider {

    public BlockModelDatagen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {

        registerEnergizedOnly("ambit_loci", BlockRegistry.AMBIT_LOCI, generator);
        registerEnergizedOnly("crafting_loci", BlockRegistry.CRAFTING_LOCI, generator);
        registerEnergizedFacing("storage_loci", BlockRegistry.STORAGE_LOCI, generator);
        registerEnergizedFacing("speed_loci", BlockRegistry.SPEED_LOCI, generator);
        registerEnergizedFacing("macro_loci", BlockRegistry.MACRO_LOCI, generator);
        registerEnergizedFacing("mute_loci", BlockRegistry.MUTE_LOCI, generator);
    }

    private static void registerEnergizedOnly(String name, Block block, BlockStateModelGenerator generator){
        var bsvNormal = BlockStateVariant.create().put(VariantSettings.MODEL, new Identifier(MOD_ID,"block/"+name));
        var bsvEnergized = BlockStateVariant.create().put(VariantSettings.MODEL, new Identifier(MOD_ID,"block/" + name + "_energized"));
        var map = BlockStateVariantMap.create(ENERGIZED).register(true,bsvEnergized).register(false, bsvNormal);

        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block).coordinate(map));
    }

    private static void registerEnergizedFacing(String name, Block block, BlockStateModelGenerator generator) {
        var bsvNormal = BlockStateVariant.create().put(VariantSettings.MODEL, new Identifier(MOD_ID, "block/" + name));
        var bsvEnergized = BlockStateVariant.create().put(VariantSettings.MODEL, new Identifier(MOD_ID, "block/" + name + "_energized"));
        var mapEnergy = BlockStateVariantMap.create(ENERGIZED).register(true, bsvEnergized).register(false, bsvNormal);

        var RotUp = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R0);
        var RotNorth = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R90);
        var RotDown = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R180);
        var RotSouth = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R270);
        var RotEast = BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R90).put(VariantSettings.X, VariantSettings.Rotation.R90);
        var RotWest = BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R270).put(VariantSettings.X, VariantSettings.Rotation.R90);

        BlockStateVariantMap mapFac = BlockStateVariantMap.create(FACING)
                .register(Direction.UP, RotUp)
                .register(Direction.NORTH, RotNorth)
                .register(Direction.DOWN, RotDown)
                .register(Direction.SOUTH, RotSouth)
                .register(Direction.EAST, RotEast)
                .register(Direction.WEST, RotWest);

        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block).coordinate(mapEnergy).coordinate(mapFac));
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {

    }
}
