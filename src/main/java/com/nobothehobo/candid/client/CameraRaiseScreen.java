package com.nobothehobo.candid.client;
import com.nobothehobo.candid.data.CameraData;
import com.nobothehobo.candid.content.CandidSounds;
import com.nobothehobo.candid.network.CameraActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
/** A brief draw-to-eye gesture only on entering the finder, never between shots. */
public final class CameraRaiseScreen extends Screen {
    public static final int DURATION=5;
    private int age;private boolean started,winding;
    public boolean winding(){return winding;}
    public CameraRaiseScreen(){super(Component.literal("Raise camera"));}
    public int animationAge(){return age;}
    @Override protected void init(){CameraOptics.enterView();if(!started){started=true;var c=CameraOptics.camera();if(CameraData.film(c)!=null&&!CameraData.isWound(c)&&CameraData.frames(c)>0){winding=true;ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.WIND,0));CandidClient.playLocal(CandidSounds.WIND);}}}
    @Override public void tick(){if(++age>=DURATION)minecraft.setScreen(new CameraScreen());}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){}
    @Override public void onClose(){minecraft.setScreen(new CameraScreen());}
}
