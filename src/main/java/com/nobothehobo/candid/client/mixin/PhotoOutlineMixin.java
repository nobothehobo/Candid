package com.nobothehobo.candid.client.mixin;
import com.nobothehobo.candid.client.CameraOptics;
import com.nobothehobo.candid.client.PhotoCapture;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Block selection is a world-render pass, not part of Gui/HUD rendering. */
@Mixin(GameRenderer.class)
public abstract class PhotoOutlineMixin {
    @Inject(method="shouldRenderBlockOutline",at=@At("HEAD"),cancellable=true)
    private void candid$cleanFrame(CallbackInfoReturnable<Boolean> cir){
        if(CameraOptics.active()||PhotoCapture.hiding())cir.setReturnValue(false);
    }
}
