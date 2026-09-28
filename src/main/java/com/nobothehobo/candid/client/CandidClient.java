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
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(com.nobothehobo.candid.content.CandidBlocks.TRIPOD_ENTITY,TripodRenderer::new);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.nobothehobo.candid.network.TripodViewPayload.ID,(payload,context)->{
            CameraOptics.mount(payload.pos(),payload.yaw(),payload.pitch());var mc=context.client();mc.setScreen(new CameraScreen(payload.release()));
        });
        ClientTickEvents.END_CLIENT_TICK.register(CameraOptics::tick);
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide()) return InteractionResult.PASS;

            if (player.getItemInHand(hand).is(CandidItems.CAMERA)) {
                CameraOptics.clearMount();
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new com.nobothehobo.candid.network.CameraActionPayload(com.nobothehobo.candid.network.CameraActionPayload.CLOSE_TRIPOD,0));
                Minecraft.getInstance().setScreen(player.isShiftKeyDown()
                        ? new CameraControlScreen()
                        : new CameraRaiseScreen());
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
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(client -> GamepadInput.shutdown());
    }

    public static void playLocal(SoundEvent sound) {
        // Physical camera feedback follows Players volume, not the UI-click slider.
        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(sound.location(),
            net.minecraft.sounds.SoundSource.PLAYERS,.85F,1.0F,
            net.minecraft.client.resources.sounds.SoundInstance.createUnseededRandom(),false,0,
            net.minecraft.client.resources.sounds.SoundInstance.Attenuation.NONE,0,0,0,true));
    }
}
