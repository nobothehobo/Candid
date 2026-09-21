package com.nobothehobo.candid.network;

import com.nobothehobo.candid.Candid;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CapturePhotoPayload(byte[] colors,int apertureIndex,int shutterIndex,String cameraId,String rollId,String shotId,double offset) implements CustomPacketPayload {
    public static final Type<CapturePhotoPayload> ID=new Type<>(Candid.id("capture_photo_v2"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CapturePhotoPayload> CODEC=StreamCodec.of((b,p)->{
        b.writeByteArray(p.colors);b.writeVarInt(p.apertureIndex);b.writeVarInt(p.shutterIndex);b.writeUtf(p.cameraId,36);b.writeUtf(p.rollId,36);b.writeUtf(p.shotId,36);b.writeDouble(p.offset);
    },b->new CapturePhotoPayload(b.readByteArray(16384),b.readVarInt(),b.readVarInt(),b.readUtf(36),b.readUtf(36),b.readUtf(36),b.readDouble()));

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
