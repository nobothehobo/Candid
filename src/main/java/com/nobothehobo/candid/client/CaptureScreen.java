package com.nobothehobo.candid.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Keep gameplay inputs captured while the HUD-free world is sampled. */
public final class CaptureScreen extends Screen {
    public CaptureScreen(){super(Component.literal("Exposing film"));}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){}
    @Override public void tick(){if(minecraft!=null)minecraft.options.keyJump.setDown(false);}
    @Override public void onClose(){PhotoCapture.cancel();net.minecraft.client.KeyMapping.releaseAll();minecraft.setScreen(new CameraScreen());}
}
