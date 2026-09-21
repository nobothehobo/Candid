package com.nobothehobo.candid.client;

import com.nobothehobo.candid.content.CandidItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;

public class CandidClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide()) return InteractionResult.PASS;

            if (player.getItemInHand(hand).is(CandidItems.CAMERA)) {
                Minecraft.getInstance().setScreen(player.isShiftKeyDown()
                        ? new CameraControlScreen()
                        : new CameraScreen());
                return InteractionResult.SUCCESS;
            }

            if (player.getItemInHand(hand).is(CandidItems.GUIDE)) {
                Minecraft.getInstance().setScreen(new GuideScreen());
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.nobothehobo.candid.network.PreviewPayload.ID,(photo,context)->{
            var mc=context.client();mc.setScreen(new NegativePreviewScreen(mc.screen,photo));
        });
        ClientTickEvents.END_CLIENT_TICK.register(PhotoCapture::tick);
    }

    public static void playLocal(SoundEvent sound) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
    }
}
