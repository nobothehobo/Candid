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

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        pollGamepad();
        g.fill(0, 0, width, height, 0xEF080808);
        int cx = width / 2;
        int cy = height / 2;
        int left = cx - 104;
        int top = cy - 55;

        g.drawCenteredString(font, "LOADING " + stock.displayName().toUpperCase(), cx, top - 28, 0xFFF0F0F0);
        g.fill(left, top, left + 208, top + 110, 0xFF252525);
        g.submitOutline(left, top, 208, 110, 0xFFAFAFAF);

        // Film chamber and take-up spool
        g.fill(left + 18, top + 22, left + 60, top + 88, 0xFF111111);
        g.submitOutline(left + 18, top + 22, 42, 66, 0xFF777777);
        g.fill(left + 151, top + 23, left + 183, top + 88, 0xFF111111);
        g.submitOutline(left + 151, top + 23, 32, 65, 0xFF777777);

        int phase = phase();
        if (phase >= 1) {
            int filmX = phase == 1 ? left + 72 - Math.min(36, Math.max(0, age - 15) * 2) : left + 35;
            g.renderItem(new ItemStack(CandidItems.itemFor(stock)), filmX, top + 46);
        }
        if (phase >= 2) {
            int leader = Math.min(92, Math.max(0, age - 34) * 6);
            g.fill(left + 54, top + 54, left + 54 + leader, top + 60, 0xFFB5823B);
        }

        // Rear door swings visually over the chamber during open/close phases.
        int doorWidth;
        if (age < 15) doorWidth = Math.max(12, 200 - age * 12);
        else if (age < 50) doorWidth = 18;
        else doorWidth = Math.min(200, 18 + (age - 50) * 13);
        g.fill(left + 4, top + 5, left + 4 + doorWidth, top + 105, 0xDD383838);
        g.submitOutline(left + 4, top + 5, doorWidth, 100, 0xFF787878);

        String step = switch (phase) {
            case 0 -> "Opening camera back…";
            case 1 -> "Dropping cartridge into the film chamber…";
            case 2 -> "Pulling leader onto the take-up spool…";
            case 3 -> "Closing and latching the back…";
            default -> "Advancing to frame 1…";
        };
        if(!acknowledged)step="Waiting for selected film — it must be in your inventory";
        g.drawCenteredString(font, step, cx, top + 121, 0xFFFFD070);
        g.drawCenteredString(font, "B / Esc skips the animation; use X to wind if needed.", cx, top + 136, 0xFFAAAAAA);
        super.render(g, mouseX, mouseY, delta);
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
