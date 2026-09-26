package com.nobothehobo.candid.client;

import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.content.CandidSounds;
import com.nobothehobo.candid.film.FilmStock;
import com.nobothehobo.candid.network.CameraActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

public class FilmLoadScreen extends Screen {
    private final FilmStock stock;
    private int age, waitingTicks;
    private boolean acknowledged;
    private boolean woundSent;
    private boolean loadSent;
    private final boolean[] pad = new boolean[15];
    private boolean padPrimed;

    public FilmLoadScreen(FilmStock stock) {
        super(Component.literal("Loading Film"));
        this.stock = stock;
    }

    @Override
    protected void init() {
        if(!loadSent){loadSent=true;ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.LOAD_FILM, stock.ordinal()));}
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void tick() {
        if(minecraft==null||minecraft.player==null)return;
        ItemStack held=minecraft.player.getMainHandItem().is(CandidItems.CAMERA)?minecraft.player.getMainHandItem():minecraft.player.getOffhandItem();
        if(com.nobothehobo.candid.data.CameraData.film(held)!=stock){
            if(++waitingTicks>100)minecraft.setScreen(new CameraControlScreen());return;
        }
        if(!acknowledged){acknowledged=true;age=0;}
        age++;
        if (age == 2) CandidClient.playLocal(CandidSounds.BACK_OPEN);
        if (age == 20) CandidClient.playLocal(CandidSounds.FILM_LOAD);
        if (age == 52) CandidClient.playLocal(CandidSounds.BACK_CLOSE);
        if (age == 68 && !woundSent) {
            woundSent = true;
            ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.WIND, 0));
            CandidClient.playLocal(CandidSounds.WIND);
        }
        if (age > 86 && minecraft != null) minecraft.setScreen(new CameraControlScreen());
    }

    public int animationAge(){return acknowledged?age:0;}
    public FilmStock stock(){return stock;}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        pollGamepad();String step=!acknowledged?"Waiting for film…":age<16?"Opening back":age<34?"Insert cartridge":age<50?"Pull leader onto spool":age<67?"Latch back":"Wind first frame";
        g.fill(0,height-46,width,height,0xc8111518);
        g.drawCenteredString(font,stock.displayName()+" • "+step,width/2,height-35,0xffeedcb9);
        g.drawCenteredString(font,"B / Esc: skip animation • film remains safe",width/2,height-18,0xffc2c5c7);
        super.render(g,x,y,delta);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(new CameraControlScreen());
    }

    private void pollGamepad() {
        if (!GLFW.glfwJoystickIsGamepad(GLFW.GLFW_JOYSTICK_1)) return;
        try (GLFWGamepadState state = GLFWGamepadState.calloc()) {
            if (!GLFW.glfwGetGamepadState(GLFW.GLFW_JOYSTICK_1, state)) return;
            if(!padPrimed){for(int i=0;i<pad.length;i++)pad[i]=state.buttons(i)==GLFW.GLFW_PRESS;padPrimed=true;return;}
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_B, this::onClose);
        }
    }

    private void edge(GLFWGamepadState state, int button, Runnable action) {
        boolean now = state.buttons(button) == GLFW.GLFW_PRESS;
        if (now && !pad[button]) action.run();
        pad[button] = now;
    }

    private int phase() {
        if (age < 15) return 0;
        if (age < 34) return 1;
        if (age < 50) return 2;
        if (age < 67) return 3;
        return 4;
    }
}
