package com.nobothehobo.candid.network;

import com.nobothehobo.candid.Candid;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Four bounded 16 KiB chunks stay below Minecraft's C2S custom-payload limit. */
public record ScanChunkPayload(String shot,int index,byte[] colors) implements CustomPacketPayload {
    public static final Type<ScanChunkPayload> ID=new Type<>(Candid.id("scan_chunk"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ScanChunkPayload> CODEC=StreamCodec.of(
        (b,p)->{b.writeUtf(p.shot,36);b.writeVarInt(p.index);b.writeByteArray(p.colors);},
        b->new ScanChunkPayload(b.readUtf(36),b.readVarInt(),b.readByteArray(16384)));
    @Override public Type<? extends CustomPacketPayload> type(){return ID;}
}
