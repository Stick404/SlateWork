package org.sophia.slate_work.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.sophia.slate_work.registries.BlockRegistry;

import java.util.Optional;

import static at.petrak.hexcasting.api.block.circle.BlockCircleComponent.ENERGIZED;
import static org.sophia.slate_work.Slate_work.MOD_ID;
import static org.sophia.slate_work.blocks.AbstractSlate.FACING;
import static org.sophia.slate_work.blocks.RedstoneLoci.POWERED;
import static org.sophia.slate_work.blocks.SaveLoci.HORIZONTAL;
import static org.sophia.slate_work.blocks.SaveLoci.TOP_PART;
import static org.sophia.slate_work.registries.BlockRegistry.ENERGIZED_BLOCKS;
import static org.sophia.slate_work.registries.BlockRegistry.SAVE_LOCI;

public class BlockModelDatagen extends FabricModelProvider {

    public BlockModelDatagen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {

        registerEnergizedOnly("ambit_loci", BlockRegistry.AMBIT_LOCI, generator);
        registerEnergizedOnly("crafting_loci", BlockRegistry.CRAFTING_LOCI, generator);
        registerEnergizedOnly("broadcaster_loci", BlockRegistry.BROADCASTER_LOCI, generator);
        registerEnergizedFacing("storage_loci", BlockRegistry.STORAGE_LOCI, generator);
        registerEnergizedFacing("speed_loci", BlockRegistry.SPEED_LOCI, generator);
        registerEnergizedFacing("macro_loci", BlockRegistry.MACRO_LOCI, generator);
        registerEnergizedFacing("mute_loci", BlockRegistry.MUTE_LOCI, generator);
        registerEnergizedFacing("sentinel_loci", BlockRegistry.SENTINEL_LOCI, generator);
        registerEnergizedFacing("hotbar_loci", BlockRegistry.HOTBAR_LOCI, generator);
        registerSaveLoci("save_loci", SAVE_LOCI, generator);
        registerRedstoneLocus("redstone_loci", BlockRegistry.REDSTONE_LOCI, generator);

        registerImpetus("listening", BlockRegistry.LISTENING_IMPETUS, generator);
    }

    private static final String impeti = "block/impeti/";

    private static void registerRedstoneLocus(String name, Block block, BlockStateModelGenerator generator){
        var bsvNormal = BlockStateVariant.create().put(VariantSettings.MODEL, new Identifier(MOD_ID, "block/" + name));
        var bsvEnergized = BlockStateVariant.create().put(VariantSettings.MODEL, new Identifier(MOD_ID, "block/" + name + "_energized"));
        var mapEnergy = BlockStateVariantMap.create(POWERED).register(true, bsvEnergized).register(false, bsvNormal);

        var RotUp = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R0);
        var RotNorth = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R90);
        var RotDown = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R180);
        var RotSouth = BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R180).put(VariantSettings.X, VariantSettings.Rotation.R90);
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

    private static void registerImpetus(String name, Block block, BlockStateModelGenerator generator){
        var path = impeti+name+"/";
        String[] pain = {"top", "bottom", "front", "back", "left", "right"};
        TextureKey[] morePain = {TextureKey.UP, TextureKey.DOWN, TextureKey.NORTH, TextureKey.SOUTH, TextureKey.WEST, TextureKey.EAST};
        TextureMap lit = new TextureMap();
        TextureMap unLit = new TextureMap();
        lit.put(TextureKey.PARTICLE, new Identifier(HexAPI.MOD_ID, "block/slate"));
        unLit.put(TextureKey.PARTICLE, new Identifier(HexAPI.MOD_ID, "block/slate"));
        int i = 0;
        for (String ouch : pain) {
            lit.put(morePain[i], new Identifier(MOD_ID, path+ouch+"_lit"));
            unLit.put(morePain[i], new Identifier(MOD_ID, path+ouch+"_dim"));
            i++;
        }
        var litModel = Models.CUBE.upload(block, "_lit", lit, generator.modelCollector);
        var dimModel = Models.CUBE.upload(block, "_dim", unLit, generator.modelCollector);

        var RotNorth = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R0);
        var RotSouth = BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R180);
        var RotUp = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R270);
        var RotDown = BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R90);
        var RotEast = BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R90);
        var RotWest = BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R270);

        BlockStateVariantMap mapFac = BlockStateVariantMap.create(FACING)
                .register(Direction.UP, RotUp)
                .register(Direction.NORTH, RotNorth)
                .register(Direction.DOWN, RotDown)
                .register(Direction.SOUTH, RotSouth)
                .register(Direction.EAST, RotEast)
                .register(Direction.WEST, RotWest);


        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block).coordinate(BlockStateVariantMap.create(ENERGIZED)
                .registerVariants(((aBoolean) -> {
                    BlockStateVariant powered;
                    if (aBoolean) {
                        powered = BlockStateVariant.create().put(VariantSettings.MODEL, litModel); } else {
                        powered = BlockStateVariant.create().put(VariantSettings.MODEL, dimModel); }
                    return java.util.List.of(powered);
                }))).coordinate(mapFac)
        );
    }

    private static void registerSaveLoci(String name, Block block, BlockStateModelGenerator generator){
        var bsvNormal = BlockStateVariant.create().put(VariantSettings.MODEL, new Identifier(MOD_ID,"block/"+name));
        var bsvEnergized = BlockStateVariant.create().put(VariantSettings.MODEL, new Identifier(MOD_ID,"block/" + name + "_energized"));

        var bsvTop = BlockStateVariant.create().put(VariantSettings.MODEL, new Identifier(MOD_ID, "block/empty"));
        var bsvBottom = BlockStateVariant.create().put(VariantSettings.MODEL, new Identifier(MOD_ID, "block/save_loci"));

        var mapTop = BlockStateVariantMap.create(TOP_PART).register(true, bsvTop).register(false, bsvBottom);
        var map = BlockStateVariantMap.create(ENERGIZED).register(true,bsvEnergized).register(false, bsvNormal);

        var facMap = BlockStateVariantMap.create(HORIZONTAL)
                .register(Direction.NORTH, BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R0))
                .register(Direction.EAST, BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R90))
                .register(Direction.SOUTH, BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R180))
                .register(Direction.WEST, BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R270));

        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block).coordinate(map).coordinate(mapTop).coordinate(facMap));
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
        var RotSouth = BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R180).put(VariantSettings.X, VariantSettings.Rotation.R90);
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
        for (var item : ENERGIZED_BLOCKS.entrySet()){
            if (item.getKey().getPath().equals("save_loci")) continue;
            if (item.getValue() instanceof BlockItem bi && bi.getBlock() instanceof BlockAbstractImpetus impetus) {
                itemModelGenerator.register(item.getValue(), new Model(
                        Optional.of(new Identifier(item.getKey().getNamespace(), "block/" + item.getKey().getPath() + "_lit")),
                        Optional.empty()
                ));
            } else {
                itemModelGenerator.register(item.getValue(), new Model(
                        Optional.of(new Identifier(item.getKey().getNamespace(), "block/" + item.getKey().getPath() + "_energized")),
                        Optional.empty()
                ));
            }
        }
        itemModelGenerator.register(SAVE_LOCI.asItem(), new Model(
                Optional.of(new Identifier(MOD_ID, "block/save_loci")),
                Optional.empty()
        ));
    }
}
