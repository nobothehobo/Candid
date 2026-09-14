package com.nobothehobo.candid.content;

import com.nobothehobo.candid.Candid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class CandidSounds {
    public static final SoundEvent SHUTTER = register("camera_shutter");
    public static final SoundEvent WIND = register("camera_wind");
    public static final SoundEvent FILM_LOAD = register("film_load");
    public static final SoundEvent BACK_OPEN = register("camera_back_open");
    public static final SoundEvent BACK_CLOSE = register("camera_back_close");

    private CandidSounds() { }

    private static SoundEvent register(String name) {
        ResourceLocation id = Candid.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void initialize() { }
}
