package com.nobothehobo.candid.client;

import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.data.CameraData;
import com.nobothehobo.candid.film.FilmStock;
import com.nobothehobo.candid.network.CameraActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

import java.util.ArrayList;
import java.util.List;

public class CameraControlScreen extends Screen {
    private static final int APERTURE = 0;
    private static final int SHUTTER = 1;
    private static final int FILM = 2;
    private static final int ADVANCE = 3;

    private int selected;
    private int apertureIndex = 4;
    private int shutterIndex = 3;
    private int filmChoice;
    private int windAnim;
    private final boolean[] pad = new boolean[15];

    public CameraControlScreen() {
        super(Component.literal("Candid Camera Controls"));
    }

    private ItemStack camera() {
        if (minecraft == null || minecraft.player == null) return ItemStack.EMPTY;
        if (minecraft.player.getMainHandItem().is(CandidItems.CAMERA)) return minecraft.player.getMainHandItem();
        if (minecraft.player.getOffhandItem().is(CandidItems.CAMERA)) return minecraft.player.getOffhandItem();
        return ItemStack.EMPTY;
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.SYNC,0));
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Rewind / unload"),b->{
            if(CameraData.film(camera())!=null&&minecraft!=null)minecraft.setScreen(new FilmUnloadScreen());
        }).bounds(width/2-72,height-28,144,20).build());
        ItemStack stack = camera();
        if (!stack.isEmpty()) {
            apertureIndex = CameraData.apertureIndex(stack);
            shutterIndex = CameraData.shutterIndex(stack);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void tick() {
        if (windAnim > 0) windAnim--;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        pollGamepad();
        ItemStack stack = camera();
        FilmStock loaded = stack.isEmpty() ? null : CameraData.film(stack);
        int frames = stack.isEmpty() ? 0 : CameraData.frames(stack);
        boolean wound = !stack.isEmpty() && CameraData.isWound(stack);

        g.fill(0, 0, width, height, 0xE20C0C0C);
        int left = width / 2 - 145;
        int top = height / 2 - 86;
        g.fill(left, top, left + 290, top + 172, 0xFF1D1D1D);
        g.submitOutline(left, top, 290, 172, 0xFFB5B5B5);
        g.fill(left + 18, top + 42, left + 272, top + 142, 0xFF2B2B2B);
        g.drawCenteredString(font, "CANDID 35 • CAMERA BODY CONTROLS", width / 2, top + 10, 0xFFF2F2F2);
        g.drawCenteredString(font, "Crouch + Use opens this screen", width / 2, top + 25, 0xFFAAAAAA);

        drawDial(g, left + 58, top + 79, "APERTURE", "f/" + trim(CameraData.APERTURES[apertureIndex]), selected == APERTURE);
        drawDial(g, left + 126, top + 79, "SHUTTER", "1/" + CameraData.SHUTTERS[shutterIndex], selected == SHUTTER);
        drawDial(g, left + 194, top + 79, "FILM", loaded == null ? filmLabel() : loaded.displayName(), selected == FILM);
        drawAdvance(g, left + 250, top + 80, wound, selected == ADVANCE);

        String status = loaded == null
                ? "No film loaded"
                : loaded.displayName() + " • ISO " + loaded.iso() + " • " + frames + "/36 • " + (wound ? "READY" : "WIND FILM");
        g.drawCenteredString(font, status, width / 2, top + 149, loaded == null ? 0xFFFFB060 : (wound ? 0xFF8CFF8C : 0xFFFFD060));
        g.drawCenteredString(font, "←→ select  ↑↓ turn  A operate  X wind  Y rewind  B close", width / 2, top + 160, 0xFFD0D0D0);
        super.render(g, mouseX, mouseY, delta);
    }

    private void drawDial(GuiGraphics g, int cx, int cy, String label, String value, boolean active) {
        int border = active ? 0xFFFFD060 : 0xFF8A8A8A;
        g.fill(cx - 23, cy - 23, cx + 24, cy + 24, 0xFF111111);
        g.submitOutline(cx - 23, cy - 23, 47, 47, border);
        g.fill(cx - 1, cy - 19, cx + 2, cy - 6, border);
        g.drawCenteredString(font, font.plainSubstrByWidth(value,60), cx, cy - 4, 0xFFFFFFFF);
        g.drawCenteredString(font, label, cx, cy + 29, active ? 0xFFFFD060 : 0xFFBFBFBF);
    }

    private void drawAdvance(GuiGraphics g, int cx, int cy, boolean wound, boolean active) {
        int border = active ? 0xFFFFD060 : 0xFF8A8A8A;
        g.fill(cx - 18, cy - 18, cx + 19, cy + 19, 0xFF111111);
        g.submitOutline(cx - 18, cy - 18, 37, 37, border);
        int lever = windAnim > 0 ? 14 : 4;
        g.fill(cx + 7, cy - 2, cx + 7 + lever, cy + 2, border);
        g.drawCenteredString(font, wound ? "READY" : "WIND", cx, cy + 25, wound ? 0xFF8CFF8C : border);
    }

    private List<FilmStock> availableFilms() {
        List<FilmStock> result = new ArrayList<>();
        if (minecraft == null || minecraft.player == null) return result;
        if (minecraft.player.getAbilities().instabuild) {
            for (FilmStock stock : FilmStock.values()) result.add(stock);
            return result;
        }
        for (int slot = 0; slot < minecraft.player.getInventory().getContainerSize(); slot++) {
            FilmStock stock = CandidItems.stockFor(minecraft.player.getInventory().getItem(slot).getItem());
            if (stock != null && !result.contains(stock)) result.add(stock);
        }
        return result;
    }

    private String filmLabel() {
        List<FilmStock> films = availableFilms();
        if (films.isEmpty()) return "NO ROLL";
        filmChoice = Math.floorMod(filmChoice, films.size());
        return films.get(filmChoice).displayName();
    }

    private void change(int direction) {
        if (selected == APERTURE) {
            apertureIndex = Math.floorMod(apertureIndex + direction, CameraData.APERTURES.length);
            ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.SET_APERTURE, apertureIndex));
        } else if (selected == SHUTTER) {
            shutterIndex = Math.floorMod(shutterIndex + direction, CameraData.SHUTTERS.length);
            ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.SET_SHUTTER, shutterIndex));
        } else if (selected == FILM) {
            List<FilmStock> films = availableFilms();
            if (!films.isEmpty()) filmChoice = Math.floorMod(filmChoice + direction, films.size());
        }
    }

    private void operate() {
        if (selected == FILM) {
            ItemStack stack = camera();
            FilmStock loaded = stack.isEmpty() ? null : CameraData.film(stack);
            if (loaded != null) return;
            List<FilmStock> films = availableFilms();
            if (films.isEmpty() || minecraft == null) return;
            filmChoice = Math.floorMod(filmChoice, films.size());
            minecraft.setScreen(new FilmLoadScreen(films.get(filmChoice)));
        } else if (selected == ADVANCE) {
            wind();
        }
    }

    private void wind() {
        ItemStack stack = camera();
        if (stack.isEmpty() || CameraData.film(stack) == null || CameraData.frames(stack) <= 0 || CameraData.isWound(stack)) return;
        ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.WIND, 0));
        windAnim = 12;
        CandidClient.playLocal(com.nobothehobo.candid.content.CandidSounds.WIND);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        return switch (input.key()) {
            case GLFW.GLFW_KEY_LEFT -> { selected = Math.floorMod(selected - 1, 4); yield true; }
            case GLFW.GLFW_KEY_RIGHT -> { selected = Math.floorMod(selected + 1, 4); yield true; }
            case GLFW.GLFW_KEY_UP -> { change(-1); yield true; }
            case GLFW.GLFW_KEY_DOWN -> { change(1); yield true; }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_SPACE -> { operate(); yield true; }
            case GLFW.GLFW_KEY_R -> { wind(); yield true; }
            case GLFW.GLFW_KEY_U -> { if(CameraData.film(camera())!=null)minecraft.setScreen(new FilmUnloadScreen()); yield true; }
            default -> super.keyPressed(input);
        };
    }

    private void pollGamepad() {
        if (!GLFW.glfwJoystickIsGamepad(GLFW.GLFW_JOYSTICK_1)) return;
        try (GLFWGamepadState state = GLFWGamepadState.calloc()) {
            if (!GLFW.glfwGetGamepadState(GLFW.GLFW_JOYSTICK_1, state)) return;
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT, () -> selected = Math.floorMod(selected - 1, 4));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT, () -> selected = Math.floorMod(selected + 1, 4));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP, () -> change(-1));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN, () -> change(1));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_A, this::operate);
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_X, this::wind);
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_Y, ()->{if(CameraData.film(camera())!=null)minecraft.setScreen(new FilmUnloadScreen());});
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_B, this::onClose);
        }
    }

    private void edge(GLFWGamepadState state, int button, Runnable action) {
        boolean now = state.buttons(button) == GLFW.GLFW_PRESS;
        if (now && !pad[button]) action.run();
        pad[button] = now;
    }

    private static String trim(float value) {
        return value == (int) value ? Integer.toString((int) value) : Float.toString(value);
    }
}
