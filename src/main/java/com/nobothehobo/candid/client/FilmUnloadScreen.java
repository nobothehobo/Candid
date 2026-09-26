package com.nobothehobo.candid.client;
import com.nobothehobo.candid.content.CandidSounds;
import com.nobothehobo.candid.network.CameraActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class FilmUnloadScreen extends Screen {
    private int ticks;private boolean sent;
    public FilmUnloadScreen(){super(Component.literal("Rewinding film"));}
    @Override protected void init(){if(!sent){sent=true;ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.UNLOAD_FILM,0));}}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void tick(){
        ticks++;if(ticks%10==1&&ticks<35)CandidClient.playLocal(CandidSounds.WIND);
        if(ticks==40)CandidClient.playLocal(CandidSounds.BACK_OPEN);
        if(ticks>58&&com.nobothehobo.candid.data.CameraData.film(CameraOptics.camera())==null)minecraft.setScreen(new CameraControlScreen());
        if(ticks>160)minecraft.setScreen(new CameraControlScreen());
    }
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        g.fill(0,0,width,height,0xEF111416);int cx=width/2,cy=height/2;
        g.drawCenteredString(font,ticks<40?"REWINDING • negatives remain protected":"OPEN BACK • return cartridge",cx,cy-36,0xFFF4E6C8);
        g.submitOutline(cx-80,cy-16,160,32,0xFF91999E);
        int length=(int)(150*Math.max(0,1-ticks/40.0));g.fill(cx-75,cy-8,cx-75+length,cy+8,0xFFB8904D);
        g.drawCenteredString(font,"Partial rolls keep their exposed frames and remaining capacity.",cx,cy+30,0xFFBBBDBA);
        super.render(g,x,y,delta);
    }
    @Override public void onClose(){if(minecraft!=null)minecraft.setScreen(new CameraControlScreen());}
}
