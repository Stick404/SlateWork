package org.sophia.slate_work.registries;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.sophia.slate_work.blocks.*;
import org.sophia.slate_work.blocks.entities.CraftingLociEntity;
import org.sophia.slate_work.blocks.entities.MacroLociEntity;
import org.sophia.slate_work.blocks.entities.SentinelLociEntity;
import org.sophia.slate_work.blocks.entities.StorageLociEntity;
import org.sophia.slate_work.item.AllayPigment;

import java.util.HashMap;

import static at.petrak.hexcasting.api.block.circle.BlockCircleComponent.ENERGIZED;
import static net.minecraft.util.Rarity.UNCOMMON;
import static org.sophia.slate_work.Slate_work.MOD_ID;

public class BlockRegistry {
    private static final AbstractBlock.Settings slateSetting = AbstractBlock.Settings.copy(Blocks.DEEPSLATE).requiresTool().strength(1.5F, 6.0F);


    private static final HashMap<Identifier, Block> BLOCK_REGISTRY = new HashMap<>();
    private static final HashMap<Identifier, Item> ITEM_REGISTRY = new HashMap<>();
    public static HashMap<Identifier, Item> ENERGIZED_BLOCKS = new HashMap<>();

    public static StorageLoci STORAGE_LOCI = registerBlockItem("storage_loci", new StorageLoci(slateSetting));
    public static CraftingLoci CRAFTING_LOCI = registerBlockItem("crafting_loci", new CraftingLoci(slateSetting.nonOpaque()));
    public static AmbitLoci AMBIT_LOCI = registerBlockItem("ambit_loci", new AmbitLoci(slateSetting));
    public static SpeedLoci SPEED_LOCI = registerBlockItem("speed_loci", new SpeedLoci(slateSetting));
    public static MacroLoci MACRO_LOCI = registerBlockItem("macro_loci", new MacroLoci(slateSetting));
    public static MuteLoci MUTE_LOCI = registerBlockItem("mute_loci", new MuteLoci(slateSetting));
    public static SentinelLoci SENTINEL_LOCI = registerBlockItem("sentinel_loci", new SentinelLoci(slateSetting));


    public static BlockEntityType<StorageLociEntity> STORAGE_LOCI_ENTITY = registerBlockEntity("storage_loci",
            FabricBlockEntityTypeBuilder.create(StorageLociEntity::new, STORAGE_LOCI).build());
    public static BlockEntityType<CraftingLociEntity> CRAFTING_LOCI_ENTITY = registerBlockEntity("crafting_loci",
            FabricBlockEntityTypeBuilder.create(CraftingLociEntity::new, CRAFTING_LOCI).build());
    public static BlockEntityType<MacroLociEntity> MACRO_LOCI_ENTITY = registerBlockEntity("macro_loci",
            FabricBlockEntityTypeBuilder.create(MacroLociEntity::new, MACRO_LOCI).build());
    public static BlockEntityType<SentinelLociEntity> SENTINEL_LOCI_ENTITY = registerBlockEntity("sentinel_loci",
            FabricBlockEntityTypeBuilder.create(SentinelLociEntity::new, SENTINEL_LOCI).build());


    public static AllayPigment ALLAY_PIGMENT = registerItem("allay_pigment", new AllayPigment(new Item.Settings().maxCount(1)));

    public static final RegistryKey<ItemGroup> SLATE_WORK_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), new Identifier(MOD_ID,"item_group"));
    public static final ItemGroup SLATE_WORK_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(AMBIT_LOCI))
            .displayName(Text.translatable("itemGroup.slate_work")).build();

    public static void init(){
        for (var e : BLOCK_REGISTRY.entrySet()){
            Registry.register(Registries.BLOCK, e.getKey(),e.getValue());
        }

        for (var e : ITEM_REGISTRY.entrySet()){
            Registry.register(Registries.ITEM, e.getKey(), e.getValue());
            if (e.getValue() instanceof BlockItem block && block.getBlock() instanceof BlockCircleComponent){
                ENERGIZED_BLOCKS.put(e.getKey(), e.getValue());
            }
            ItemGroupEvents.modifyEntriesEvent(SLATE_WORK_GROUP_KEY).register(group -> group.add(e.getValue()));
        }

        Registry.register(Registries.ITEM_GROUP, SLATE_WORK_GROUP_KEY, SLATE_WORK_GROUP);
    }


    private static <T extends Block> T registerBlockItem(String name, T block){
        BLOCK_REGISTRY.put(new Identifier(MOD_ID,name), block);
        ITEM_REGISTRY.put(new Identifier(MOD_ID,name), new BlockItem(block.getDefaultState().with(ENERGIZED, true).getBlock(), new Item.Settings().rarity(UNCOMMON)));
        return block;
    }

    private static <T extends BlockEntityType<?>> T registerBlockEntity(String name, T blockEntityType){
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID,name), blockEntityType);
    }

    private static <T extends Item> T registerItem(String name, T item){
        ITEM_REGISTRY.put(new Identifier(MOD_ID,name), item);
        return item;
    }
}
