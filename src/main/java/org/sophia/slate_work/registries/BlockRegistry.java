package org.sophia.slate_work.registries;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.sophia.slate_work.blocks.StorageLoci;
import org.sophia.slate_work.blocks.StorageLociEntity;

import java.util.HashMap;

import static org.sophia.slate_work.Slate_work.MOD_ID;

public class BlockRegistry {
    private static final HashMap<Identifier, Block> BLOCK_REGISTRY = new HashMap<>();
    private static final HashMap<Identifier, Block> ITEM_REGISTRY = new HashMap<>();

    public static StorageLoci STORAGE_LOCI = registerBlockItem("storage_slate",new StorageLoci(
            AbstractBlock.Settings.copy(Blocks.DEEPSLATE_TILES).strength(4f, 4f)));

    public static BlockEntityType<StorageLociEntity> STORAGE_LOCI_ENTITY = registerBlockEntity("storage_slate",
            FabricBlockEntityTypeBuilder.create(StorageLociEntity::new,STORAGE_LOCI).build());


    public static void init(){
        for (var e : BLOCK_REGISTRY.entrySet()){
            Registry.register(Registries.BLOCK, e.getKey(),e.getValue());
        }

        for (var e : ITEM_REGISTRY.entrySet()){
            Registry.register(Registries.ITEM, e.getKey(), new BlockItem(e.getValue(), new Item.Settings()));
        }
    }


    private static <T extends Block> T registerBlock(String name, T block){
        BLOCK_REGISTRY.put(new Identifier(MOD_ID,name), block);
        return block;
    }

    private static <T extends Block> T registerBlockItem(String name, T block){
        BLOCK_REGISTRY.put(new Identifier(MOD_ID,name), block);
        ITEM_REGISTRY.put(new Identifier(MOD_ID,name), block);
        return block;
    }

    private static <T extends BlockEntityType<?>> T registerBlockEntity(String name, T blockEntityType){
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID,name), blockEntityType);
    }
}
