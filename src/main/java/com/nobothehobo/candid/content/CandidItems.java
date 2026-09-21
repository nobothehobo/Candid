package com.nobothehobo.candid.content;

import com.nobothehobo.candid.Candid;
import com.nobothehobo.candid.film.FilmStock;
import com.nobothehobo.candid.item.CameraItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public final class CandidItems {
    public static final Item CAMERA = register("camera", CameraItem::new, new Item.Properties().stacksTo(1));

    // Keep existing registry IDs for save compatibility while improving their real-world-inspired profiles.
    public static final Item FILM_DAYLIGHT_100 = register("film_daylight_100", com.nobothehobo.candid.item.FilmRollItem::new, new Item.Properties().stacksTo(16));
    public static final Item FILM_SUN_200 = register("film_sun_200", com.nobothehobo.candid.item.FilmRollItem::new, new Item.Properties().stacksTo(16));
    public static final Item FILM_EVERYDAY_400 = register("film_everyday_400", com.nobothehobo.candid.item.FilmRollItem::new, new Item.Properties().stacksTo(16));
    public static final Item FILM_PORTRAIT_400 = register("film_portrait_400", com.nobothehobo.candid.item.FilmRollItem::new, new Item.Properties().stacksTo(16));
    public static final Item FILM_NIGHT_800 = register("film_night_800", com.nobothehobo.candid.item.FilmRollItem::new, new Item.Properties().stacksTo(16));
    public static final Item FILM_MONO_400 = register("film_mono_400", com.nobothehobo.candid.item.FilmRollItem::new, new Item.Properties().stacksTo(16));
    public static final Item FILM_FINE_MONO_400 = register("film_fine_mono_400", com.nobothehobo.candid.item.FilmRollItem::new, new Item.Properties().stacksTo(16));

    public static final Item DEVELOPER = register("developer", Item::new, new Item.Properties().stacksTo(16));
    public static final Item PHOTO_PAPER = register("photo_paper", Item::new, new Item.Properties().stacksTo(64));
    public static final Item GUIDE = register("guide", Item::new, new Item.Properties().stacksTo(1));

    public static final ResourceKey<CreativeModeTab> GROUP_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(), Candid.id("photography"));
    public static final CreativeModeTab GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(CAMERA))
            .title(Component.translatable("itemGroup.candid.photography"))
            .build();

    private CandidItems() { }

    private static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Candid.id(name));
        T item = factory.apply(properties.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static FilmStock stockFor(Item item) {
        if (item == FILM_DAYLIGHT_100) return FilmStock.DAYLIGHT_100;
        if (item == FILM_SUN_200) return FilmStock.WARM_200;
        if (item == FILM_EVERYDAY_400) return FilmStock.EVERYDAY_400;
        if (item == FILM_PORTRAIT_400) return FilmStock.PORTRAIT_400;
        if (item == FILM_NIGHT_800) return FilmStock.NIGHT_800;
        if (item == FILM_MONO_400) return FilmStock.MONO_400;
        if (item == FILM_FINE_MONO_400) return FilmStock.FINE_MONO_400;
        return null;
    }

    public static Item itemFor(FilmStock stock) {
        return switch (stock) {
            case DAYLIGHT_100 -> FILM_DAYLIGHT_100;
            case WARM_200 -> FILM_SUN_200;
            case EVERYDAY_400 -> FILM_EVERYDAY_400;
            case PORTRAIT_400 -> FILM_PORTRAIT_400;
            case NIGHT_800 -> FILM_NIGHT_800;
            case MONO_400 -> FILM_MONO_400;
            case FINE_MONO_400 -> FILM_FINE_MONO_400;
        };
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, GROUP_KEY, GROUP);
        ItemGroupEvents.modifyEntriesEvent(GROUP_KEY).register(entries -> {
            entries.accept(CAMERA);
            entries.accept(FILM_DAYLIGHT_100);
            entries.accept(FILM_SUN_200);
            entries.accept(FILM_EVERYDAY_400);
            entries.accept(FILM_PORTRAIT_400);
            entries.accept(FILM_NIGHT_800);
            entries.accept(FILM_MONO_400);
            entries.accept(FILM_FINE_MONO_400);
            entries.accept(DEVELOPER);
            entries.accept(PHOTO_PAPER);
            entries.accept(GUIDE);
        });
    }
}
