package com.nobothehobo.candid.client.mixin;

import com.nobothehobo.candid.client.CameraOptics;
import com.nobothehobo.candid.data.CameraData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class TripodCameraMixin {
    @Shadow protected abstract void setPosition(Vec3 pos);
    @Inject(method="setup",at=@At("TAIL"))
    private void candid$tripod(CallbackInfo ci){
        var p=Minecraft.getInstance().player;
        if(CameraOptics.active()&&p!=null&&CameraData.tripod(CameraOptics.camera(),p)!=null)setPosition(CameraOptics.anchor());
    }
}
