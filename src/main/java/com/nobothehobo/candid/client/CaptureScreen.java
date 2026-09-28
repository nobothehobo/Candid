package com.nobothehobo.candid.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Keep gameplay inputs captured while the HUD-free world is sampled. */
public final class CaptureScreen extends Screen {
    public CaptureScreen(){super(Component.literal("Exposing film"));}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        // Never draw a shutter into a framebuffer that is still being photographed.
        if(!PhotoCapture.exposureComplete())return;
        var frame=com.nobothehobo.candid.core.FrameGeometry.of(width,height);
        float progress=PhotoCapture.mirrorProgress();
        int curtain=(int)(frame.height()*(progress<.65?1:(1-progress)/.35));
        g.fill(frame.x(),frame.y(),frame.x()+frame.width(),frame.y()+curtain,0xff08090b);
    }
    @Override public void tick(){if(minecraft!=null){
        minecraft.options.keyJump.setDown(false);
        if(PhotoCapture.exposureComplete()&&PhotoCapture.mirrorProgress()>=1)minecraft.setScreen(new CameraScreen());
    }}
    @Override public void onClose(){PhotoCapture.cancel();net.minecraft.client.KeyMapping.releaseAll();minecraft.setScreen(new CameraScreen());}
}
