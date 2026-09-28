package com.nobothehobo.candid.client.mixin;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import com.nobothehobo.candid.photo.TripodTarget;
import com.nobothehobo.candid.network.TripodUsePayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
public abstract class TripodUseMixin {
    @org.spongepowered.asm.mixin.Shadow private int rightClickDelay;
    @Inject(method="startUseItem",at=@At("HEAD"),cancellable=true)
    private void candid$cameraTarget(CallbackInfo ci){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.screen!=null||mc.player.isHandsBusy()||mc.gameMode==null||mc.gameMode.isDestroying())return;
        var pos=TripodTarget.find(mc.player);
        if(pos!=null&&ClientPlayNetworking.canSend(TripodUsePayload.ID)){
            ClientPlayNetworking.send(new TripodUsePayload(pos));rightClickDelay=4;ci.cancel();
        }
    }
}
