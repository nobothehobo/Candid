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
    private final SceneMeter sceneMeter=new SceneMeter();
    private long lastAim=System.nanoTime();
    private final boolean[] pad = new boolean[15];
    private boolean padPrimed;

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
        ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.SYNC,0));
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

    // The finder is an optical overlay, not a menu: preserve the sharp world image.
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) { }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        pollGamepad();
        ItemStack camera = camera();
        FilmStock stock = camera.isEmpty() ? null : CameraData.film(camera);
        int frames = camera.isEmpty() ? 0 : CameraData.frames(camera);
        boolean wound = !camera.isEmpty() && CameraData.isWound(camera);
        float meter = meterStops(stock);

        var frame=com.nobothehobo.candid.core.FrameGeometry.of(width,height);
        int cx=width/2,cy=height/2;
        graphics.fill(0,0,width,frame.y(),0xCA090B0D);
        graphics.fill(0,frame.y()+frame.height(),width,height,0xCA090B0D);
        graphics.fill(0,frame.y(),frame.x(),frame.y()+frame.height(),0xA9090B0D);
        graphics.fill(frame.x()+frame.width(),frame.y(),width,frame.y()+frame.height(),0xA9090B0D);
        graphics.submitOutline(frame.x(),frame.y(),frame.width(),frame.height(),0xF2F4EBD2);
        double target=sceneMeter.subjectDistance();
        boolean inFocus=com.nobothehobo.candid.core.Optics.blurRadius(CameraData.lens(camera),CameraData.APERTURES[apertureIndex],CameraData.focus(camera),target,504)<.8;
        graphics.submitOutline(cx-14,cy-10,28,20,inFocus?0xff97d9ab:0xffeed29c);
        graphics.drawCenteredString(font,target>=1000?"Subject: infinity":"Subject: "+String.format(java.util.Locale.ROOT,"%.1f m",target),cx,frame.y()+frame.height()-12,0xffe6dfce);
        graphics.fill(cx-5,cy,cx+6,cy+1,0xCCFFFFFF);
        graphics.fill(cx,cy-5,cx+1,cy+6,0xCCFFFFFF);
        graphics.drawCenteredString(font,CameraData.lens(camera)+" mm • focus "+(CameraData.focus(camera)>=1000?"infinity":CameraData.focus(camera)+" m")+" • "+(CameraData.tripod(camera,minecraft.player)==null?"handheld":"tripod"),cx,23,0xFFB7B3A5);
        String filmText = stock == null
                ? "NO FILM • Crouch + Use for camera controls"
                : stock.displayName() + "  ISO " + stock.iso() + "  " + frames + "/36  " + (wound ? "READY" : "WIND");
        graphics.drawCenteredString(font, filmText, cx, 9,
                stock == null ? 0xFFFF7070 : (wound ? 0xFFFFFFFF : 0xFFFFD060));

        String exposure = "f/" + trim(CameraData.APERTURES[apertureIndex]) + "     " + CameraData.shutterLabel(CameraData.SHUTTERS[shutterIndex]) + String.format(java.util.Locale.ROOT,"    %+.1f EV",meter);
        graphics.drawCenteredString(font, exposure, cx, height - 48, 0xFFFFFFFF);
        drawMeter(graphics, cx, height - 31, meter);
        if(Math.abs(meter)>=1)graphics.drawCenteredString(font,meter>0?"Overexposed • brighter, softer highlights":"Underexposed • darker shadows, more grain",cx,frame.y()+frame.height()+8,0xffebcf94);
        drawWindLever(graphics, width - 45, height - 38, wound);

        graphics.drawCenteredString(font, GLFW.glfwJoystickIsGamepad(GLFW.GLFW_JOYSTICK_1)
                ? "D-pad: exposure • A: shoot • X: wind • Y: controls"
                : "Arrows: exposure • Enter: shoot • R: wind • C: controls", cx, height-10, 0xFFCCCCCC);

        if (stock == null) {
            graphics.drawCenteredString(font, "Load a roll from Crouch + Use / Y controls", cx, cy + 45, 0xFFFFC070);
        } else if (!wound && frames > 0) {
            graphics.drawCenteredString(font, "Advance film before the next exposure • X", cx, cy + 45, 0xFFFFD060);
        } else if (frames <= 0) {
            graphics.drawCenteredString(font, "Roll finished • open controls and REWIND / UNLOAD", cx, cy + 45, 0xFFFF9090);
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
        double sceneEv=sceneMeter.read(minecraft);
        return (float)com.nobothehobo.candid.core.Exposure.offset(sceneEv,
            new com.nobothehobo.candid.core.Exposure.Settings(CameraData.APERTURES[apertureIndex],CameraData.SHUTTERS[shutterIndex]),stock.iso());
    }

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
        if(PhotoCapture.queue(camera, apertureIndex, shutterIndex, meterStops(stock))) minecraft.setScreen(new CaptureScreen());
    }

    private void focusSubject(){
        double distance=sceneMeter.subjectDistance(),best=Double.MAX_VALUE;int chosen=0;
        for(int i=0;i<com.nobothehobo.candid.core.Optics.FOCUS.length;i++){double error=Math.abs(1/distance-1/com.nobothehobo.candid.core.Optics.FOCUS[i]);if(error<best){best=error;chosen=i;}}
        ClientPlayNetworking.send(new CameraActionPayload(CameraActionPayload.FOCUS,chosen));
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
            case GLFW.GLFW_KEY_LEFT_BRACKET -> {CameraOptics.focus(-1);yield true;}
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> {CameraOptics.focus(1);yield true;}
            case GLFW.GLFW_KEY_F -> {focusSubject();yield true;}
            case GLFW.GLFW_KEY_C -> { openControls(); yield true; }
            default -> super.keyPressed(input);
        };
    }

    private void pollGamepad() {
        if (!GLFW.glfwJoystickIsGamepad(GLFW.GLFW_JOYSTICK_1)) return;
        try (GLFWGamepadState state = GLFWGamepadState.calloc()) {
            if (!GLFW.glfwGetGamepadState(GLFW.GLFW_JOYSTICK_1, state)) return;
            if(!padPrimed){for(int i=0;i<pad.length;i++)pad[i]=state.buttons(i)==GLFW.GLFW_PRESS;padPrimed=true;return;}
            long now=System.nanoTime();double dt=Math.min(.05,(now-lastAim)/1e9);lastAim=now;
            if(minecraft!=null&&minecraft.player!=null){
                float ax=state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X),ay=state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y);
                if(Math.abs(ax)>.18)minecraft.player.setYRot(minecraft.player.getYRot()+(float)(ax*70*dt));
                if(Math.abs(ay)>.18)minecraft.player.setXRot(Math.max(-89,Math.min(89,minecraft.player.getXRot()+(float)(ay*55*dt))));
            }
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP, () -> changeAperture(-1));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN, () -> changeAperture(1));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT, () -> changeShutter(-1));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT, () -> changeShutter(1));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER, ()->CameraOptics.focus(-1));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER, ()->CameraOptics.focus(1));
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB, this::focusSubject);
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_A, this::shoot);
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_X, this::wind);
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_Y, this::openControls);
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_B, this::onClose);
        }
    }

    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(vertical!=0)CameraOptics.focus(vertical>0?-1:1);return true;}
    private double lastMouseX,lastMouseY;
    @Override public void mouseMoved(double x,double y){
        if(minecraft!=null&&minecraft.player!=null&&GLFW.glfwGetMouseButton(minecraft.getWindow().handle(),GLFW.GLFW_MOUSE_BUTTON_RIGHT)==GLFW.GLFW_PRESS){
            minecraft.player.setYRot(minecraft.player.getYRot()+(float)((x-lastMouseX)*.22));
            minecraft.player.setXRot(Math.max(-89,Math.min(89,minecraft.player.getXRot()+(float)((y-lastMouseY)*.22))));
        }
        lastMouseX=x;lastMouseY=y;super.mouseMoved(x,y);
    }
    private void edge(GLFWGamepadState state, int button, Runnable action) {
        boolean now = state.buttons(button) == GLFW.GLFW_PRESS;
        if (now && !pad[button]) action.run();
        pad[button] = now;
    }
}
