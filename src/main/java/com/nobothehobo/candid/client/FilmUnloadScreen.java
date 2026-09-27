package com.nobothehobo.candid.client;
import com.nobothehobo.candid.content.CandidSounds;
import com.nobothehobo.candid.network.CameraActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class FilmUnloadScreen extends Screen {
    private int ticks;private boolean sent;
    private final com.nobothehobo.candid.film.FilmStock stock=com.nobothehobo.candid.data.CameraData.film(CameraOptics.camera());
    public int animationAge(){return ticks;}
    public com.nobothehobo.candid.film.FilmStock stock(){return stock==null?com.nobothehobo.candid.film.FilmStock.WARM_200:stock;}
    public FilmUnloadScreen(){super(Component.literal("Rewinding film"));}
    @Override protected void init(){if(!sent){sent=true;ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.UNLOAD_FILM,0));}}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void tick(){
        ticks++;if(ticks%10==1&&ticks<35)CandidClient.playLocal(CandidSounds.WIND);
        if(ticks==40)CandidClient.playLocal(CandidSounds.BACK_OPEN);
        if(ticks>58&&com.nobothehobo.candid.data.CameraData.film(CameraOptics.camera())==null)minecraft.setScreen(new CameraControlScreen());
        if(ticks>160)minecraft.setScreen(new CameraControlScreen());
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        String step=ticks<10?"Press rewind release":ticks<40?"Rewind film into cartridge":ticks<50?"Open back • lift cartridge":"Close camera back";
        g.fill(0,height-46,width,height,0xc8111518);
        g.drawCenteredString(font,step,width/2,height-35,0xffeedcb9);
        g.drawCenteredString(font,"Esc: skip • exposed frames and remaining capacity stay safe",width/2,height-18,0xffc2c5c7);
        super.render(g,x,y,delta);
    }
    @Override public void onClose(){if(minecraft!=null)minecraft.setScreen(new CameraControlScreen());}
}
