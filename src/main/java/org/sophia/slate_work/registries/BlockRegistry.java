package org.sophia.slate_work.registries;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
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
import org.sophia.slate_work.blocks.entities.*;
import org.sophia.slate_work.blocks.impetus.ListeningImpetus;
import org.sophia.slate_work.blocks.impetus.ListeningImpetusEntity;
import org.sophia.slate_work.item.AllayPigment;
import org.sophia.slate_work.item.WhisperingStone;

import java.util.HashMap;

import static at.petrak.hexcasting.api.block.circle.BlockCircleComponent.ENERGIZED;
import static net.minecraft.util.Rarity.UNCOMMON;
import static org.sophia.slate_work.Slate_work.MOD_ID;

public class BlockRegistry {
    private static final AbstractBlock.Settings slateSetting = AbstractBlock.Settings.copy(Blocks.DEEPSLATE).requiresTool().strength(1.5F, 6.0F);
    private static final AbstractBlock.Settings locusSetting = slateSetting.pistonBehavior(PistonBehavior.DESTROY);


    private static final HashMap<Identifier, Block> BLOCK_REGISTRY = new HashMap<>();
    private static final HashMap<Identifier, Item> ITEM_REGISTRY = new HashMap<>();
    public static HashMap<Identifier, Item> ENERGIZED_BLOCKS = new HashMap<>();

    public static StorageLoci STORAGE_LOCI = registerBlockItem("storage_loci", new StorageLoci(locusSetting));
    public static CraftingLoci CRAFTING_LOCI = registerBlockItem("crafting_loci", new CraftingLoci(locusSetting.nonOpaque()));
    public static AmbitLoci AMBIT_LOCI = registerBlockItem("ambit_loci", new AmbitLoci(locusSetting));
    public static SpeedLoci SPEED_LOCI = registerBlockItem("speed_loci", new SpeedLoci(locusSetting));
    public static MacroLoci MACRO_LOCI = registerBlockItem("macro_loci", new MacroLoci(locusSetting));
    public static MuteLoci MUTE_LOCI = registerBlockItem("mute_loci", new MuteLoci(locusSetting));
    public static SentinelLoci SENTINEL_LOCI = registerBlockItem("sentinel_loci", new SentinelLoci(locusSetting));
    public static BroadcasterLoci BROADCASTER_LOCI = registerBlockItem("broadcaster_loci", new BroadcasterLoci(locusSetting));
    public static SaveLoci SAVE_LOCI = registerBlockItem("save_loci", new SaveLoci(locusSetting.pistonBehavior(PistonBehavior.BLOCK)));
    public static HotbarLoci HOTBAR_LOCI = registerBlockItem("hotbar_loci", new HotbarLoci(locusSetting));
    public static RedstoneLoci REDSTONE_LOCI = registerBlockItem("redstone_loci", new RedstoneLoci(locusSetting));

    public static ListeningImpetus LISTENING_IMPETUS = registerBlockItem("listening_impetus", new ListeningImpetus(locusSetting.pistonBehavior(PistonBehavior.BLOCK)));


    public static BlockEntityType<StorageLociEntity> STORAGE_LOCI_ENTITY = registerBlockEntity("storage_loci",
            FabricBlockEntityTypeBuilder.create(StorageLociEntity::new, STORAGE_LOCI).build());
    public static BlockEntityType<CraftingLociEntity> CRAFTING_LOCI_ENTITY = registerBlockEntity("crafting_loci",
            FabricBlockEntityTypeBuilder.create(CraftingLociEntity::new, CRAFTING_LOCI).build());
    public static BlockEntityType<MacroLociEntity> MACRO_LOCI_ENTITY = registerBlockEntity("macro_loci",
            FabricBlockEntityTypeBuilder.create(MacroLociEntity::new, MACRO_LOCI).build());
    public static BlockEntityType<SentinelLociEntity> SENTINEL_LOCI_ENTITY = registerBlockEntity("sentinel_loci",
            FabricBlockEntityTypeBuilder.create(SentinelLociEntity::new, SENTINEL_LOCI).build());
    public static BlockEntityType<BroadcasterLociEntity> BROADCASTER_LOCI_ENTITY = registerBlockEntity("broadcaster_loci",
            FabricBlockEntityTypeBuilder.create(BroadcasterLociEntity::new, BROADCASTER_LOCI).build());
    public static BlockEntityType<SaveLociEntity> SAVE_LOCI_ENTITY = registerBlockEntity("save_loci",
            FabricBlockEntityTypeBuilder.create(SaveLociEntity::new, SAVE_LOCI).build());
    public static BlockEntityType<HotbarLociEntity> HOTBAR_LOCI_ENTITY = registerBlockEntity("hotbar_loci",
            FabricBlockEntityTypeBuilder.create(HotbarLociEntity::new, HOTBAR_LOCI).build());
    
    public static BlockEntityType<ListeningImpetusEntity> LISTENING_IMPETUS_ENTITY = registerBlockEntity("listening_impetus",
            FabricBlockEntityTypeBuilder.create(ListeningImpetusEntity::new, LISTENING_IMPETUS).build());


    public static AllayPigment ALLAY_PIGMENT = registerItem("allay_pigment", new AllayPigment(new Item.Settings().maxCount(1)));
    public static WhisperingStone WHISPERING_STONE = registerItem("whispering_stone", new WhisperingStone(new Item.Settings().maxCount(1)));

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
