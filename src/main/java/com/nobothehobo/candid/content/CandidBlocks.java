package com.nobothehobo.candid.content;

import com.nobothehobo.candid.Candid;
import com.nobothehobo.candid.block.DarkroomBasinBlock;
import com.nobothehobo.candid.block.EnlargerBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class CandidBlocks {
    public static final Block DARKROOM_BASIN = register("darkroom_basin", DarkroomBasinBlock::new,
            BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.METAL).noOcclusion());
    public static final Block ENLARGER = register("enlarger", EnlargerBlock::new,
            BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.METAL).lightLevel(state -> 4));

    private CandidBlocks() { }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Candid.id(name));
        Block block = factory.apply(props.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Candid.id(name));
        Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
        return block;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CandidItems.GROUP_KEY).register(entries -> {
            entries.accept(DARKROOM_BASIN.asItem());
            entries.accept(ENLARGER.asItem());
        });
    }
}
