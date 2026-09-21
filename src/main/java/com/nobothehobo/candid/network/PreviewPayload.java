package com.nobothehobo.candid.network;

import com.nobothehobo.candid.Candid;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PreviewPayload(byte[] colors,String title,byte[] png,String filename) implements CustomPacketPayload {
    public static final Type<PreviewPayload> ID=new Type<>(Candid.id("negative_preview"));
    public static final StreamCodec<RegistryFriendlyByteBuf,PreviewPayload> CODEC=StreamCodec.of(
        (b,p)->{b.writeByteArray(p.colors);b.writeUtf(p.title,256);b.writeByteArray(p.png);b.writeUtf(p.filename,64);},
        b->new PreviewPayload(b.readByteArray(65536),b.readUtf(256),b.readByteArray(524288),b.readUtf(64)));
    @Override public Type<? extends CustomPacketPayload> type(){return ID;}
}
