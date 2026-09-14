package com.nobothehobo.candid.network;

import com.nobothehobo.candid.Candid;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CapturePhotoPayload(byte[] colors, int apertureIndex, int shutterIndex) implements CustomPacketPayload {
    public static final Type<CapturePhotoPayload> ID = new Type<>(Candid.id("capture_photo"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CapturePhotoPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.byteArray(16384), CapturePhotoPayload::colors,
            ByteBufCodecs.VAR_INT, CapturePhotoPayload::apertureIndex,
            ByteBufCodecs.VAR_INT, CapturePhotoPayload::shutterIndex,
            CapturePhotoPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
