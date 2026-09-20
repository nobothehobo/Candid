package com.nobothehobo.candid.photo;

import com.nobothehobo.candid.film.FilmStock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public final class PhotoMaps {
    private static final String EXPOSED = "candid_exposed";
    private static final String DEVELOPED = "candid_developed";
    private static final String FILM = "candid_stock";
    private static final String ISO = "candid_iso";
    private static final String APERTURE = "candid_f";
    private static final String SHUTTER = "candid_shutter";
    private static final byte[] NEGATIVE_LOOKUP = buildNegativeLookup();

    private PhotoMaps() { }

    public static ItemStack createPrint(ServerPlayer player, byte[] colors) {
        if(colors.length!=16384)throw new IllegalArgumentException("Invalid photo size");
        ItemStack map=MapItem.create(player.level(),0,0,(byte)0,false,false);
        MapItemSavedData data=MapItem.getSavedData(map,player.level()).locked();
        System.arraycopy(colors,0,data.colors,0,colors.length);data.setDirty();
        player.level().setMapData(map.get(DataComponents.MAP_ID),data);return map;
    }
    public static void markDeveloped(ItemStack map) {CustomData.update(DataComponents.CUSTOM_DATA,map,t->t.putBoolean(DEVELOPED,true));}

    public static ItemStack createNegative(ServerPlayer player, byte[] positive, FilmStock stock, float aperture, int shutter) {
        ServerLevel level = player.level();
        ItemStack map = MapItem.create(level, player.getBlockX(), player.getBlockZ(), (byte) 0, false, false);
        MapItemSavedData data = MapItem.getSavedData(map, level);
        if (data != null) {
            for (int i = 0; i < data.colors.length && i < positive.length; i++) {
                data.colors[i] = NEGATIVE_LOOKUP[positive[i] & 0xFF];
            }
            data.setDirty();
        }
        map.set(DataComponents.CUSTOM_NAME, Component.literal("Exposed " + stock.displayName() + " Negative"));
        CustomData.update(DataComponents.CUSTOM_DATA, map, tag -> {
            tag.putBoolean(EXPOSED, true);
            tag.putBoolean(DEVELOPED, false);
            tag.putString(FILM, stock.displayName());
            tag.putInt(ISO, stock.iso());
            tag.putFloat(APERTURE, aperture);
            tag.putInt(SHUTTER, shutter);
        });
        return map;
    }

    public static boolean isExposed(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBooleanOr(EXPOSED, false) && !tag.getBooleanOr(DEVELOPED, false);
    }

    public static boolean isDeveloped(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBooleanOr(DEVELOPED, false);
    }

    public static boolean develop(ItemStack stack, ServerLevel level) {
        if (!isExposed(stack)) return false;
        MapItemSavedData data = MapItem.getSavedData(stack, level);
        if (data == null) return false;
        for (int i = 0; i < data.colors.length; i++) data.colors[i] = NEGATIVE_LOOKUP[data.colors[i] & 0xFF];
        data.setDirty();

        CompoundTag old = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String film = old.getStringOr(FILM, "Candid Film");
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Candid Print • " + film));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(EXPOSED, false);
            tag.putBoolean(DEVELOPED, true);
        });
        return true;
    }

    public static ItemStack duplicatePrint(ItemStack developed) {
        return developed.copyWithCount(1);
    }

    private static byte[] buildNegativeLookup() {
        byte[] lookup = new byte[256];
        int[] palette = new int[256];
        for (int i = 0; i < 256; i++) palette[i] = MapColor.getColorFromPackedId(i);
        for (int i = 0; i < 256; i++) {
            int rgb = palette[i];
            int r = 255 - ((rgb >> 16) & 255);
            int g = 255 - ((rgb >> 8) & 255);
            int b = 255 - (rgb & 255);
            lookup[i] = (byte) nearest(r, g, b, palette);
        }
        return lookup;
    }

    private static int nearest(int r, int g, int b, int[] palette) {
        int best = 4;
        long bestDistance = Long.MAX_VALUE;
        for (int i = 4; i < palette.length; i++) {
            int c = palette[i];
            int cr = (c >> 16) & 255;
            int cg = (c >> 8) & 255;
            int cb = c & 255;
            long dr = r - cr, dg = g - cg, db = b - cb;
            long d = dr * dr + dg * dg + db * db;
            if (d < bestDistance) { bestDistance = d; best = i; }
        }
        return best;
    }
}
