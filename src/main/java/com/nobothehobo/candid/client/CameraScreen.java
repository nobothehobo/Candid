package com.nobothehobo.candid.client;

import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.content.CandidSounds;
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

public class CameraScreen extends Screen {
    private int apertureIndex = 4;
    private int shutterIndex = 3;
    private int windAnim;
    private final boolean[] pad = new boolean[15];

    public CameraScreen() {
        super(Component.literal("Candid Viewfinder"));
    }

    private ItemStack camera() {
        if (minecraft == null || minecraft.player == null) return ItemStack.EMPTY;
        if (minecraft.player.getMainHandItem().is(CandidItems.CAMERA)) return minecraft.player.getMainHandItem();
        if (minecraft.player.getOffhandItem().is(CandidItems.CAMERA)) return minecraft.player.getOffhandItem();
        return ItemStack.EMPTY;
    }

    @Override
    protected void init() {
        ItemStack camera = camera();
        if (!camera.isEmpty()) {
            apertureIndex = CameraData.apertureIndex(camera);
            shutterIndex = CameraData.shutterIndex(camera);
        }
    }

    @Override
    public void tick() {
        if (windAnim > 0) windAnim--;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        pollGamepad();
        ItemStack camera = camera();
        FilmStock stock = camera.isEmpty() ? null : CameraData.film(camera);
        int frames = camera.isEmpty() ? 0 : CameraData.frames(camera);
        boolean wound = !camera.isEmpty() && CameraData.isWound(camera);
        float meter = meterStops(stock);

        graphics.fill(0, 0, width, 28, 0xB9000000);
        graphics.fill(0, height - 46, width, height, 0xC9000000);
        graphics.fill(0, 0, 20, height, 0x9A000000);
        graphics.fill(width - 20, 0, width, height, 0x9A000000);

        int cx = width / 2;
        int cy = height / 2;
        graphics.submitOutline(cx - 38, cy - 26, 76, 52, 0xCCFFFFFF);
        graphics.submitOutline(cx - 9, cy - 9, 18, 18, 0xAAFFFFFF);
        graphics.fill(cx - 1, cy - 8, cx + 1, cy + 9, 0x99FFFFFF);
        graphics.fill(cx - 8, cy - 1, cx + 9, cy + 1, 0x99FFFFFF);

        String filmText = stock == null
                ? "NO FILM • Crouch + Use for camera controls"
                : stock.displayName() + "  ISO " + stock.iso() + "  " + frames + "/36  " + (wound ? "READY" : "WIND");
        graphics.drawCenteredString(font, filmText, cx, 9,
                stock == null ? 0xFFFF7070 : (wound ? 0xFFFFFFFF : 0xFFFFD060));

        String exposure = "f/" + trim(CameraData.APERTURES[apertureIndex]) + "     1/" + CameraData.SHUTTERS[shutterIndex];
        graphics.drawCenteredString(font, exposure, cx, height - 37, 0xFFFFFFFF);
        drawMeter(graphics, cx, height - 21, meter);
        drawWindLever(graphics, width - 45, height - 34, wound);

        graphics.drawString(font, "D-pad ↑↓ aperture  ←→ shutter", 28, height - 14, 0xFFCCCCCC, false);
        graphics.drawString(font, "A shutter  X wind  Y controls  B close", width - 207, height - 14, 0xFFCCCCCC, false);

        if (stock == null) {
            graphics.drawCenteredString(font, "Load a roll from Crouch + Use / Y controls", cx, cy + 45, 0xFFFFC070);
        } else if (!wound && frames > 0) {
            graphics.drawCenteredString(font, "Advance film before the next exposure • X", cx, cy + 45, 0xFFFFD060);
        } else if (frames <= 0) {
            graphics.drawCenteredString(font, "Roll finished • open camera controls to load another", cx, cy + 45, 0xFFFF9090);
        }
        super.render(graphics, mouseX, mouseY, delta);
    }

    private void drawWindLever(GuiGraphics graphics, int x, int y, boolean wound) {
        int color = wound ? 0xFF8CFF8C : 0xFFFFD060;
        graphics.submitOutline(x - 10, y - 8, 22, 16, color);
        int length = windAnim > 0 ? 18 : 7;
        graphics.fill(x + 4, y - 1, x + 4 + length, y + 2, color);
    }

    private void drawMeter(GuiGraphics graphics, int cx, int y, float stops) {
        int start = cx - 72;
        graphics.drawString(font, "−3", start - 15, y - 4, 0xFFCCCCCC, false);
        graphics.drawString(font, "0", cx - 3, y - 4, 0xFFFFFFFF, false);
        graphics.drawString(font, "+3", cx + 76, y - 4, 0xFFCCCCCC, false);
        graphics.fill(start, y + 7, cx + 72, y + 8, 0x88FFFFFF);
        for (int i = -3; i <= 3; i++) {
            int x = cx + i * 24;
            graphics.fill(x, y + 3, x + 1, y + 12, 0xCCFFFFFF);
        }
        int needle = cx + Math.round(Math.max(-3, Math.min(3, stops)) * 24);
        graphics.fill(needle - 2, y, needle + 3, y + 12, Math.abs(stops) <= 0.35f ? 0xFF70FF70 : 0xFFFFD65A);
    }

    private float meterStops(FilmStock stock) {
        if (stock == null || minecraft == null || minecraft.player == null || minecraft.level == null) return 0f;
        int light = minecraft.level.getMaxLocalRawBrightness(minecraft.player.blockPosition());
        double sceneEv = 1.5 + (light / 15.0) * 13.5;
        double n = CameraData.APERTURES[apertureIndex];
        double shutter = CameraData.SHUTTERS[shutterIndex];
        double cameraEv = log2(n * n * shutter) - log2(stock.iso() / 100.0);
        return (float) (sceneEv - cameraEv);
    }

    private static double log2(double v) { return Math.log(v) / Math.log(2.0); }
    private static String trim(float value) { return value == (int) value ? Integer.toString((int) value) : Float.toString(value); }

    private void changeAperture(int delta) {
        apertureIndex = Math.floorMod(apertureIndex + delta, CameraData.APERTURES.length);
        ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.SET_APERTURE, apertureIndex));
    }

    private void changeShutter(int delta) {
        shutterIndex = Math.floorMod(shutterIndex + delta, CameraData.SHUTTERS.length);
        ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.SET_SHUTTER, shutterIndex));
    }

    private void wind() {
        ItemStack camera = camera();
        if (camera.isEmpty() || CameraData.film(camera) == null || CameraData.frames(camera) <= 0 || CameraData.isWound(camera)) return;
        ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.WIND, 0));
        windAnim = 12;
        CandidClient.playLocal(CandidSounds.WIND);
    }

    private void shoot() {
        ItemStack camera = camera();
        FilmStock stock = camera.isEmpty() ? null : CameraData.film(camera);
        if (stock == null || CameraData.frames(camera) <= 0 || minecraft == null) return;
        if (!CameraData.isWound(camera)) {
            if (minecraft.player != null) minecraft.player.displayClientMessage(Component.literal("Wind the film first • X / R"), true);
            return;
        }
        CandidClient.playLocal(CandidSounds.SHUTTER);
        PhotoCapture.queue(camera, apertureIndex, shutterIndex, meterStops(stock));
        minecraft.setScreen(null);
    }

    private void openControls() {
        if (minecraft != null) minecraft.setScreen(new CameraControlScreen());
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        return switch (input.key()) {
            case GLFW.GLFW_KEY_UP -> { changeAperture(-1); yield true; }
            case GLFW.GLFW_KEY_DOWN -> { changeAperture(1); yield true; }
            case GLFW.GLFW_KEY_LEFT -> { changeShutter(-1); yield true; }
            case GLFW.GLFW_KEY_RIGHT -> { changeShutter(1); yield true; }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_SPACE -> { shoot(); yield true; }
            case GLFW.GLFW_KEY_R -> { wind(); yield true; }
            case GLFW.GLFW_KEY_C -> { openControls(); yield true; }
            default -> super.keyPressed(input);
        };
    }

    private void pollGamepad() {
        if (!GLFW.glfwJoystickIsGamepad(GLFW.GLFW_JOYSTICK_1)) return;
        try (GLFWGamepadState state = GLFWGamepadState.calloc()) {
            if (!GLFW.glfwGetGamepadState(GLFW.GLFW_JOYSTICK_1, state)) return;
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP, () -> changeAperture(-1));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN, () -> changeAperture(1));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT, () -> changeShutter(-1));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT, () -> changeShutter(1));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_A, this::shoot);
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_X, this::wind);
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_Y, this::openControls);
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_B, this::onClose);
        }
    }

    private void edge(GLFWGamepadState state, int button, Runnable action) {
        boolean now = state.buttons(button) == GLFW.GLFW_PRESS;
        if (now && !pad[button]) action.run();
        pad[button] = now;
    }
}
