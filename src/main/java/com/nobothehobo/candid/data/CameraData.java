package com.nobothehobo.candid.data;

import com.nobothehobo.candid.film.FilmStock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class CameraData {
    public static final int FRAME_COUNT = 36;
    public static final float[] APERTURES = {1.4f, 2.0f, 2.8f, 4.0f, 5.6f, 8.0f, 11.0f, 16.0f};
    public static final int[] SHUTTERS = {15, 30, 60, 125, 250, 500, 1000, 8, 4, 2, -1, -2, -4, -8, -15, -30};

    private static final String FILM = "candid_film";
    private static final String FRAMES = "candid_frames";
    private static final String APERTURE = "candid_aperture";
    private static final String SHUTTER = "candid_shutter";
    private static final String WOUND = "candid_wound";

    public static String shutterLabel(int value){return value>0?"1/"+value:Math.abs(value)+" s";}
    public static int lensIndex(ItemStack s){return Math.floorMod(tag(s).getIntOr("candid_lens",1),4);}
    public static int lens(ItemStack s){return com.nobothehobo.candid.core.Optics.LENSES[lensIndex(s)];}
    public static int focusIndex(ItemStack s){return Math.floorMod(tag(s).getIntOr("candid_focus",11),12);}
    public static double focus(ItemStack s){return com.nobothehobo.candid.core.Optics.FOCUS[focusIndex(s)];}
    public static void setFocus(ItemStack s,int index){CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putInt("candid_focus",Math.floorMod(index,12)));}
    public static void setLens(ItemStack s,int index){CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putInt("candid_lens",Math.floorMod(index,4)));}
    public static void mount(ItemStack s,net.minecraft.core.BlockPos pos){CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putInt("candid_tripod_x",pos.getX());t.putInt("candid_tripod_y",pos.getY());t.putInt("candid_tripod_z",pos.getZ());t.putBoolean("candid_mounted",true);});}
    public static net.minecraft.core.BlockPos tripod(ItemStack s,net.minecraft.world.entity.player.Player p){
        var t=tag(s);if(!t.getBooleanOr("candid_mounted",false))return null;
        var pos=new net.minecraft.core.BlockPos(t.getIntOr("candid_tripod_x",0),t.getIntOr("candid_tripod_y",0),t.getIntOr("candid_tripod_z",0));
        return p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<16&&p.level().getBlockState(pos).is(com.nobothehobo.candid.content.CandidBlocks.TRIPOD)?pos:null;
    }
    public static void unmount(ItemStack s){CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putBoolean("candid_mounted",false));}
    private CameraData() { }

    private static CompoundTag tag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static FilmStock film(ItemStack stack) {
        return FilmStock.byName(tag(stack).getStringOr(FILM, ""));
    }

    public static int frames(ItemStack stack) {
        return Math.max(0, Math.min(FRAME_COUNT, tag(stack).getIntOr(FRAMES, 0)));
    }

    public static int apertureIndex(ItemStack stack) {
        return Math.floorMod(tag(stack).getIntOr(APERTURE, 4), APERTURES.length);
    }

    public static int shutterIndex(ItemStack stack) {
        return Math.floorMod(tag(stack).getIntOr(SHUTTER, 3), SHUTTERS.length);
    }

    public static boolean isWound(ItemStack stack) {
        return tag(stack).getBooleanOr(WOUND, false);
    }

    public static float aperture(ItemStack stack) { return APERTURES[apertureIndex(stack)]; }
    public static int shutter(ItemStack stack) { return SHUTTERS[shutterIndex(stack)]; }

    public static void load(ItemStack stack, FilmStock film) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(FILM, film.name());
            tag.putInt(FRAMES, FRAME_COUNT);
            tag.putBoolean(WOUND, false);
            if (!tag.contains(APERTURE)) tag.putInt(APERTURE, 4);
            if (!tag.contains(SHUTTER)) tag.putInt(SHUTTER, 3);
        });
    }

    public static void clearFilm(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(FILM);
            tag.remove("candid_roll_id");
            tag.putInt(FRAMES, 0);
            tag.putBoolean(WOUND, false);
        });
    }

    public static String cameraId(ItemStack stack) { return tag(stack).getStringOr("candid_camera_id", ""); }
    public static String rollId(ItemStack stack) { return tag(stack).getStringOr("candid_roll_id", ""); }
    public static void identify(ItemStack stack) {
        if(cameraId(stack).isEmpty()) CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putString("candid_camera_id",java.util.UUID.randomUUID().toString()));
    }
    public static void bind(ItemStack camera, com.nobothehobo.candid.core.RollState roll) {
        CustomData.update(DataComponents.CUSTOM_DATA,camera,t->{t.putString("candid_roll_id",roll.id().toString());t.putString(FILM,roll.stock());t.putInt(FRAMES,36-roll.used());});
    }

    public static boolean wind(ItemStack stack) {
        if (film(stack) == null || frames(stack) <= 0 || isWound(stack)) return false;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(WOUND, true));
        return true;
    }

    public static boolean consumeFrame(ItemStack stack) {
        int current = frames(stack);
        if (current <= 0 || !isWound(stack)) return false;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(FRAMES, current - 1);
            tag.putBoolean(WOUND, false);
        });
        return true;
    }

    public static void setApertureIndex(ItemStack stack, int index) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(APERTURE, Math.floorMod(index, APERTURES.length)));
    }

    public static void setShutterIndex(ItemStack stack, int index) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(SHUTTER, Math.floorMod(index, SHUTTERS.length)));
    }
}
