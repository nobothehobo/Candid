package com.nobothehobo.candid.client.mixin;
import com.nobothehobo.candid.client.CameraScreen;
import com.nobothehobo.candid.client.PhotoCapture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Gui.class)
public abstract class PhotoHudMixin {
    @Inject(method="render",at=@At("HEAD"),cancellable=true)
    private void candid$hideHud(CallbackInfo ci){if(Minecraft.getInstance().screen instanceof CameraScreen||PhotoCapture.hiding())ci.cancel();}
}
