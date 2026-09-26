package com.nobothehobo.candid.network;

import com.nobothehobo.candid.Candid;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Bounded 16 KiB chunks: map proof (kind 0), full-color PNG scan (kind 1). */
public record ScanChunkPayload(String shot,int kind,int total,int index,byte[] colors) implements CustomPacketPayload {
    public ScanChunkPayload(String shot,int index,byte[] colors){this(shot,0,65536,index,colors);}
    public static final Type<ScanChunkPayload> ID=new Type<>(Candid.id("scan_chunk_v2"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ScanChunkPayload> CODEC=StreamCodec.of(
        (b,p)->{b.writeUtf(p.shot,36);b.writeVarInt(p.kind);b.writeVarInt(p.total);b.writeVarInt(p.index);b.writeByteArray(p.colors);},
        b->new ScanChunkPayload(b.readUtf(36),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readByteArray(16384)));
    @Override public Type<? extends CustomPacketPayload> type(){return ID;}
}
