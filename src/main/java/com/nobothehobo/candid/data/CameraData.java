package com.nobothehobo.candid.data;

import com.nobothehobo.candid.film.FilmStock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class CameraData {
    public static final int FRAME_COUNT = 36;
    public static final float[] APERTURES = {1.4f, 2.0f, 2.8f, 4.0f, 5.6f, 8.0f, 11.0f, 16.0f};
    public static final int[] SHUTTERS = {15, 30, 60, 125, 250, 500, 1000};

    private static final String FILM = "candid_film";
    private static final String FRAMES = "candid_frames";
    private static final String APERTURE = "candid_aperture";
    private static final String SHUTTER = "candid_shutter";

    private CameraData() { }

    private static CompoundTag tag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static FilmStock film(ItemStack stack) {
        return FilmStock.byName(tag(stack).getStringOr(FILM, ""));
    }

    public static int frames(ItemStack stack) {
        return tag(stack).getIntOr(FRAMES, 0);
    }

    public static int apertureIndex(ItemStack stack) {
        return Math.floorMod(tag(stack).getIntOr(APERTURE, 4), APERTURES.length);
    }

    public static int shutterIndex(ItemStack stack) {
        return Math.floorMod(tag(stack).getIntOr(SHUTTER, 3), SHUTTERS.length);
    }

    public static float aperture(ItemStack stack) { return APERTURES[apertureIndex(stack)]; }
    public static int shutter(ItemStack stack) { return SHUTTERS[shutterIndex(stack)]; }

    public static void load(ItemStack stack, FilmStock film) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(FILM, film.name());
            tag.putInt(FRAMES, FRAME_COUNT);
            if (!tag.contains(APERTURE)) tag.putInt(APERTURE, 4);
            if (!tag.contains(SHUTTER)) tag.putInt(SHUTTER, 3);
        });
    }

    public static void clearFilm(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(FILM);
            tag.putInt(FRAMES, 0);
        });
    }

    public static boolean consumeFrame(ItemStack stack) {
        int current = frames(stack);
        if (current <= 0) return false;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(FRAMES, current - 1));
        return true;
    }

    public static void setApertureIndex(ItemStack stack, int index) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(APERTURE, Math.floorMod(index, APERTURES.length)));
    }

    public static void setShutterIndex(ItemStack stack, int index) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(SHUTTER, Math.floorMod(index, SHUTTERS.length)));
    }
}
