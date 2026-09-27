package com.nobothehobo.candid.network;
import com.nobothehobo.candid.Candid;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record TripodViewPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<TripodViewPayload> ID=new Type<>(Candid.id("tripod_view"));
    public static final StreamCodec<RegistryFriendlyByteBuf,TripodViewPayload> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,TripodViewPayload::pos,TripodViewPayload::new);
    @Override public Type<? extends CustomPacketPayload> type(){return ID;}
}
