package com.nobothehobo.candid.client;

import com.nobothehobo.candid.content.*;
import com.nobothehobo.candid.data.CameraData;
import com.nobothehobo.candid.core.Optics;
import com.nobothehobo.candid.film.FilmStock;
import com.nobothehobo.candid.network.CameraActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.*;
import java.util.*;

public final class CameraControlScreen extends Screen {
    private final List<Button> buttons=new ArrayList<>();
    private final List<Runnable> actions=new ArrayList<>();
    private final boolean[] pad=new boolean[15];
    private boolean primed;
    private int selected,stockIndex,lensChoice;
    private Button aperture,shutter,film,load,lens,focus;
    public CameraControlScreen(){super(Component.literal("Candid camera controls"));}
    private void send(int action,int value){ClientPlayNetworking.send(new CameraActionPayload(action,value));}
    @Override protected void init(){
        send(CameraActionPayload.SYNC,0);buttons.clear();actions.clear();
        var c=CameraOptics.camera();lensChoice=CameraData.lensIndex(c);
        int left=width/2-150,top=Math.max(26,height/2-98);
        aperture=button("Aperture",left,top,146,()->send(CameraActionPayload.SET_APERTURE,CameraData.apertureIndex(CameraOptics.camera())+1));
        shutter=button("Shutter",left+154,top,146,()->send(CameraActionPayload.SET_SHUTTER,CameraData.shutterIndex(CameraOptics.camera())+1));
        film=button("Film stock",left,top+25,146,()->stockIndex=(stockIndex+1)%FilmStock.values().length);
        load=button("Load film",left+154,top+25,146,this::loadOrUnload);
        button("Wind / advance",left,top+50,146,()->{send(CameraActionPayload.WIND,0);CandidClient.playLocal(CandidSounds.WIND);});
        button("Open viewfinder",left+154,top+50,146,()->minecraft.setScreen(new CameraScreen()));
        lens=button("Lens",left,top+75,146,()->lensChoice=(lensChoice+1)%4);
        button("Attach selected lens",left+154,top+75,146,()->send(CameraActionPayload.LENS,lensChoice));
        focus=button("Focus",left,top+100,146,()->CameraOptics.focus(1));
        button("Focus nearer",left+154,top+100,146,()->CameraOptics.focus(-1));
        button("Detach tripod",left,top+125,146,()->send(CameraActionPayload.UNMOUNT,0));
        button("Field guide",left+154,top+125,146,()->minecraft.setScreen(new GuideScreen()));
        button("Done",left,top+154,300,this::onClose);
        setInitialFocus(buttons.get(Math.min(selected,buttons.size()-1)));refresh();
    }
    private Button button(String text,int x,int y,int w,Runnable run){var b=Button.builder(Component.literal(text),v->run.run()).bounds(x,y,w,20).build();buttons.add(b);actions.add(run);return addRenderableWidget(b);}
    private void loadOrUnload(){
        if(PhotoCapture.busy())return;
        var stock=CameraData.film(CameraOptics.camera());
        minecraft.setScreen(stock==null?new FilmLoadScreen(FilmStock.values()[stockIndex]):new FilmUnloadScreen());
    }
    private void refresh(){
        var c=CameraOptics.camera();
        aperture.setMessage(Component.literal("Aperture  f/"+CameraData.aperture(c)+"  +"));
        shutter.setMessage(Component.literal("Shutter  "+CameraData.shutterLabel(CameraData.shutter(c))+"  +"));
        film.setMessage(Component.literal(FilmStock.values()[stockIndex].displayName()+"  >"));
        load.setMessage(Component.literal(CameraData.film(c)==null?"Load selected film":"Rewind / unload roll"));
        lens.setMessage(Component.literal(Optics.LENSES[lensChoice]+" mm lens  >"));
        double distance=CameraData.focus(c);focus.setMessage(Component.literal("Focus  "+(distance>=1000?"infinity":distance+" m")+"  +"));
    }
    @Override public void tick(){refresh();}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        poll();g.fill(0,0,width,height,0xf0171a1c);
        var c=CameraOptics.camera();var stock=CameraData.film(c);
        String status=stock==null?"CANDID 35 • no film":stock.displayName()+" • "+CameraData.frames(c)+" left • "+(CameraData.isWound(c)?"ready":"wind film");
        g.drawCenteredString(font,status,width/2,10,0xffe9ddc5);
        g.drawCenteredString(font,"Click / Tab + Enter • D-pad + A • wheel in finder: focus",width/2,height-12,0xffb7b9b8);
        super.render(g,x,y,delta);
    }
    @Override public boolean keyPressed(KeyEvent e){
        if(e.key()==GLFW.GLFW_KEY_U){loadOrUnload();return true;}
        if(e.key()==GLFW.GLFW_KEY_R){send(CameraActionPayload.WIND,0);return true;}
        return super.keyPressed(e);
    }
    private void poll(){
        if(!GLFW.glfwJoystickIsGamepad(0))return;
        try(var s=GLFWGamepadState.calloc()){
            if(!GLFW.glfwGetGamepadState(0,s))return;
            if(!primed){for(int i=0;i<15;i++)pad[i]=s.buttons(i)==GLFW.GLFW_PRESS;primed=true;return;}
            edge(s,GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN,()->select(2));edge(s,GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP,()->select(-2));
            edge(s,GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT,()->select(-1));edge(s,GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT,()->select(1));
            edge(s,GLFW.GLFW_GAMEPAD_BUTTON_A,()->actions.get(selected).run());
            edge(s,GLFW.GLFW_GAMEPAD_BUTTON_Y,this::loadOrUnload);edge(s,GLFW.GLFW_GAMEPAD_BUTTON_B,this::onClose);
        }
    }
    private void select(int d){selected=Math.floorMod(selected+d,buttons.size());setFocused(buttons.get(selected));}
    private void edge(GLFWGamepadState s,int i,Runnable r){boolean held=s.buttons(i)==GLFW.GLFW_PRESS;if(held&&!pad[i])r.run();pad[i]=held;}
}
