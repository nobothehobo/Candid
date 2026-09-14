package com.nobothehobo.candid;

import com.nobothehobo.candid.content.CandidBlocks;
import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.data.CameraData;
import com.nobothehobo.candid.film.FilmStock;
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

        PayloadTypeRegistry.playC2S().register(CapturePhotoPayload.ID, CapturePhotoPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CapturePhotoPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            ItemStack camera = player.getMainHandItem();
            if (!camera.is(CandidItems.CAMERA)) camera = player.getOffhandItem();
            if (!camera.is(CandidItems.CAMERA)) return;
            FilmStock stock = CameraData.film(camera);
            if (stock == null || CameraData.frames(camera) <= 0 || payload.colors().length != 16384) return;

            int apertureIndex = Math.floorMod(payload.apertureIndex(), CameraData.APERTURES.length);
            int shutterIndex = Math.floorMod(payload.shutterIndex(), CameraData.SHUTTERS.length);
            CameraData.setApertureIndex(camera, apertureIndex);
            CameraData.setShutterIndex(camera, shutterIndex);
            if (!CameraData.consumeFrame(camera)) return;

            ItemStack negative = PhotoMaps.createNegative(player, payload.colors(), stock,
                    CameraData.APERTURES[apertureIndex], CameraData.SHUTTERS[shutterIndex]);
            if (!player.getInventory().add(negative)) player.drop(negative, false);
        });
    }
}
