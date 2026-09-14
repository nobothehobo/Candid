package com.nobothehobo.candid.client;

import com.nobothehobo.candid.content.CandidItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;

public class CandidClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide()) return InteractionResult.PASS;
            if (player.getItemInHand(hand).is(CandidItems.CAMERA) && !player.isShiftKeyDown()) {
                net.minecraft.client.Minecraft.getInstance().setScreen(new CameraScreen());
                return InteractionResult.SUCCESS;
            }
            if (player.getItemInHand(hand).is(CandidItems.GUIDE)) {
                net.minecraft.client.Minecraft.getInstance().setScreen(new GuideScreen());
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(PhotoCapture::tick);
    }
}
