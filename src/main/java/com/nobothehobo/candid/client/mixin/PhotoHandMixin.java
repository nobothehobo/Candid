package com.nobothehobo.candid.client.mixin;
import com.nobothehobo.candid.client.CameraScreen;
import com.nobothehobo.candid.client.PhotoCapture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemInHandRenderer.class)
public abstract class PhotoHandMixin {
    @Inject(method="renderHandsWithItems",at=@At("HEAD"),cancellable=true)
    private void candid$hideHands(CallbackInfo ci){if(Minecraft.getInstance().screen instanceof CameraScreen||PhotoCapture.hiding())ci.cancel();}
}
