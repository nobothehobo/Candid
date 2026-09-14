package com.nobothehobo.candid.network;

import com.nobothehobo.candid.Candid;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CameraActionPayload(int action, int value) implements CustomPacketPayload {
    public static final int SET_APERTURE = 0;
    public static final int SET_SHUTTER = 1;
    public static final int WIND = 2;
    public static final int LOAD_FILM = 3;

    public static final Type<CameraActionPayload> ID = new Type<>(Candid.id("camera_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CameraActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CameraActionPayload::action,
            ByteBufCodecs.VAR_INT, CameraActionPayload::value,
            CameraActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
