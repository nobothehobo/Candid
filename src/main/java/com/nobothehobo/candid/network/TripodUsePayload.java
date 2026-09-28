package com.nobothehobo.candid.network;
import com.nobothehobo.candid.Candid;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record TripodUsePayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<TripodUsePayload> ID=new Type<>(Candid.id("tripod_use"));
    public static final StreamCodec<RegistryFriendlyByteBuf,TripodUsePayload> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,TripodUsePayload::pos,TripodUsePayload::new);
    @Override public Type<? extends CustomPacketPayload> type(){return ID;}
}
