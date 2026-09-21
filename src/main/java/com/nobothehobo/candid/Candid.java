package com.nobothehobo.candid;

import com.nobothehobo.candid.content.CandidBlocks;
import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.content.CandidSounds;
import com.nobothehobo.candid.data.CameraData;
import com.nobothehobo.candid.film.FilmStock;
import com.nobothehobo.candid.network.CameraActionPayload;
import com.nobothehobo.candid.network.CapturePhotoPayload;
import com.nobothehobo.candid.photo.PhotoMaps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class Candid implements ModInitializer {
    public static final String MOD_ID = "candid";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        CandidItems.initialize();
        CandidBlocks.initialize();
        CandidSounds.initialize();

        PayloadTypeRegistry.playC2S().register(CapturePhotoPayload.ID, CapturePhotoPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CameraActionPayload.ID, CameraActionPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(com.nobothehobo.candid.network.PreviewPayload.ID,com.nobothehobo.candid.network.PreviewPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(com.nobothehobo.candid.network.ScanChunkPayload.ID,com.nobothehobo.candid.network.ScanChunkPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(com.nobothehobo.candid.network.ScanChunkPayload.ID,(payload,context)->{
            if(!cameraInHands(context.player()).isEmpty())com.nobothehobo.candid.photo.ScanUploads.accept(context.player(),payload);
        });
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->com.nobothehobo.candid.photo.ScanUploads.clear(handler.player.getUUID()));
        com.nobothehobo.candid.photo.RollManager.initialize();
        ServerPlayNetworking.registerGlobalReceiver(CameraActionPayload.ID, (payload, context) -> {
            ServerPlayer player=context.player(); ItemStack camera=cameraInHands(player); if(camera.isEmpty())return;
            com.nobothehobo.candid.photo.RollManager.safely(player,()->{
                com.nobothehobo.candid.photo.RollManager.sync(player,camera);
                switch(payload.action()) {
                    case CameraActionPayload.SET_APERTURE -> CameraData.setApertureIndex(camera,payload.value());
                    case CameraActionPayload.SET_SHUTTER -> CameraData.setShutterIndex(camera,payload.value());
                    case CameraActionPayload.WIND -> CameraData.wind(camera);
                    case CameraActionPayload.LOAD_FILM -> com.nobothehobo.candid.photo.RollManager.load(player,camera,payload.value());
                    case CameraActionPayload.UNLOAD_FILM -> com.nobothehobo.candid.photo.RollManager.unload(player,camera);
                    default -> { }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(CapturePhotoPayload.ID,(payload,context)->{
            ServerPlayer player=context.player();ItemStack camera=cameraInHands(player);if(camera.isEmpty())return;
            com.nobothehobo.candid.photo.RollManager.safely(player,()->com.nobothehobo.candid.photo.RollManager.capture(player,camera,payload));
        });
    }

    private static ItemStack cameraInHands(ServerPlayer player) {
        if (player.getMainHandItem().is(CandidItems.CAMERA)) return player.getMainHandItem();
        if (player.getOffhandItem().is(CandidItems.CAMERA)) return player.getOffhandItem();
        return ItemStack.EMPTY;
    }

}
