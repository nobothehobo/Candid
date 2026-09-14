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

        ServerPlayNetworking.registerGlobalReceiver(CameraActionPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            ItemStack camera = cameraInHands(player);
            if (camera.isEmpty()) return;

            switch (payload.action()) {
                case CameraActionPayload.SET_APERTURE ->
                        CameraData.setApertureIndex(camera, payload.value());
                case CameraActionPayload.SET_SHUTTER ->
                        CameraData.setShutterIndex(camera, payload.value());
                case CameraActionPayload.WIND ->
                        CameraData.wind(camera);
                case CameraActionPayload.LOAD_FILM ->
                        loadFilm(player, camera, payload.value());
                default -> { }
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(CapturePhotoPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            ItemStack camera = cameraInHands(player);
            if (camera.isEmpty()) return;

            FilmStock stock = CameraData.film(camera);
            if (stock == null || CameraData.frames(camera) <= 0 || !CameraData.isWound(camera)
                    || payload.colors().length != 16384) return;

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

    private static ItemStack cameraInHands(ServerPlayer player) {
        if (player.getMainHandItem().is(CandidItems.CAMERA)) return player.getMainHandItem();
        if (player.getOffhandItem().is(CandidItems.CAMERA)) return player.getOffhandItem();
        return ItemStack.EMPTY;
    }

    private static void loadFilm(ServerPlayer player, ItemStack camera, int ordinal) {
        FilmStock current = CameraData.film(camera);
        if (current != null && CameraData.frames(camera) > 0) return;

        FilmStock[] stocks = FilmStock.values();
        if (ordinal < 0 || ordinal >= stocks.length) return;
        FilmStock wanted = stocks[ordinal];

        if (player.getAbilities().instabuild) {
            CameraData.load(camera, wanted);
            return;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (CandidItems.stockFor(candidate.getItem()) == wanted && !candidate.isEmpty()) {
                candidate.shrink(1);
                CameraData.load(camera, wanted);
                return;
            }
        }
    }
}
