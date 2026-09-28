package com.nobothehobo.candid.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Keep gameplay inputs captured while the HUD-free world is sampled. */
public final class CaptureScreen extends Screen {
    private boolean padPrimed,previousCancel;
    public CaptureScreen(){super(Component.literal("Exposing film"));}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        // Never draw a shutter into a framebuffer that is still being photographed.
        var frame=com.nobothehobo.candid.core.FrameGeometry.of(width,height);
        if(!PhotoCapture.exposureComplete()){
            double duration=PhotoCapture.exposureSeconds();
            if(duration>=1&&frame.y()>=24){
                // These margins are outside the shared photo crop, including at scaled GUI sizes.
                g.fill(0,0,width,frame.y(),0xff090b0d);
                g.drawCenteredString(font,String.format(java.util.Locale.ROOT,"Exposing • %.1f / %.0f s",Math.min(duration,PhotoCapture.elapsedSeconds()),duration),width/2,frame.y()-22,0xffeedcb9);
                g.drawCenteredString(font,"Esc / B: cancel • no frame used",width/2,frame.y()-11,0xffc2c5c7);
            }
            return;
        }
        float progress=PhotoCapture.mirrorProgress();
        int curtain=(int)(frame.height()*(progress<.65?1:(1-progress)/.35));
        g.fill(frame.x(),frame.y(),frame.x()+frame.width(),frame.y()+curtain,0xff08090b);
    }
    @Override public void tick(){if(minecraft!=null){
        minecraft.options.keyJump.setDown(false);
        try(var state=GamepadInput.read()){
            boolean cancel=state.connected()&&state.buttons(GamepadInput.B)==1;
            if(padPrimed&&cancel&&!previousCancel){onClose();return;}
            previousCancel=cancel;padPrimed=true;
        }
        if(PhotoCapture.exposureComplete()&&PhotoCapture.mirrorProgress()>=1)minecraft.setScreen(new CameraScreen());
    }}
    @Override public void onClose(){PhotoCapture.cancel();net.minecraft.client.KeyMapping.releaseAll();minecraft.setScreen(new CameraScreen());}
}
