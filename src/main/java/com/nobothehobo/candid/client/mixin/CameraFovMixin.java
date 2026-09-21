package com.nobothehobo.candid.client.mixin;

import com.nobothehobo.candid.client.CameraOptics;
import com.nobothehobo.candid.data.CameraData;
import com.nobothehobo.candid.core.Optics;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class CameraFovMixin {
    @Inject(method="getFov",at=@At("RETURN"),cancellable=true)
    private void candid$fov(Camera camera,float partialTick,boolean changing,CallbackInfoReturnable<Float> result){
        if(CameraOptics.active()){
            var window=net.minecraft.client.Minecraft.getInstance().getWindow();
            double fraction=(double)com.nobothehobo.candid.core.FrameGeometry.of(window.getWidth(),window.getHeight()).height()/window.getHeight();
            result.setReturnValue((float)Math.toDegrees(2*Math.atan(Math.tan(Math.toRadians(Optics.verticalFov(CameraData.lens(CameraOptics.camera())))/2)/fraction)));
        }
    }
}
