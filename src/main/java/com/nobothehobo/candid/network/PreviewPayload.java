package com.nobothehobo.candid.network;

import com.nobothehobo.candid.Candid;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PreviewPayload(byte[] colors,String title) implements CustomPacketPayload {
    public static final Type<PreviewPayload> ID=new Type<>(Candid.id("negative_preview"));
    public static final StreamCodec<RegistryFriendlyByteBuf,PreviewPayload> CODEC=StreamCodec.of(
        (b,p)->{b.writeByteArray(p.colors);b.writeUtf(p.title,256);},
        b->new PreviewPayload(b.readByteArray(65536),b.readUtf(256)));
    @Override public Type<? extends CustomPacketPayload> type(){return ID;}
}
