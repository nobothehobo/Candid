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

public class CameraScreen extends Screen {
    private int apertureIndex = 4;
    private int shutterIndex = 3;
    private int windAnim;
    private boolean releaseOnOpen;
    private final SceneMeter sceneMeter=new SceneMeter();
    private long lastAim=System.nanoTime();
    private final boolean[] pad = new boolean[15];
    private boolean padPrimed;

    public CameraScreen() {
        super(Component.literal("Candid Viewfinder"));
    }
    public CameraScreen(boolean release){this();releaseOnOpen=release;}

    private ItemStack camera() {
        return CameraOptics.camera();
    }

    @Override
    protected void init() {
        CameraOptics.enterView();
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
        PhotoCapture.prepareFocus(minecraft);
        if(releaseOnOpen&&!camera().isEmpty()){releaseOnOpen=false;shoot();}
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
        var focusRange=com.nobothehobo.candid.core.Optics.focusRange(CameraData.lens(camera),CameraData.APERTURES[apertureIndex],CameraData.focus(camera));
        boolean inFocus=focusRange.contains(target>=1000?Double.POSITIVE_INFINITY:target);
        graphics.submitOutline(cx-14,cy-10,28,20,inFocus?0xff97d9ab:0xffeed29c);
        double focus=CameraData.focus(camera);
        int split=inFocus?0:(int)Math.copySign(Math.max(2,Math.min(10,Math.abs(1/focus-1/target)*35)),focus-target);
        int focusColor=inFocus?0xff97d9ab:0xffeed29c;
        graphics.fill(cx-6+split,cy-7,cx+7+split,cy-5,focusColor);
        graphics.fill(cx-6-split,cy+5,cx+7-split,cy+7,focusColor);
        String focusHint=(inFocus?"In focus":focus<target?"Focus farther →":"← Focus nearer")+" • "+(GamepadInput.present()?"LB/RB • L3: match":"Wheel / [ ] • F: match");
        graphics.drawCenteredString(font,focusHint,cx,cy+16,focusColor);
        graphics.drawCenteredString(font,target>=1000?"Subject: infinity":"Subject: "+String.format(java.util.Locale.ROOT,"%.1f m",target),cx,frame.y()+frame.height()-12,0xffe6dfce);
        graphics.fill(cx-5,cy,cx+6,cy+1,0xCCFFFFFF);
        graphics.fill(cx,cy-5,cx+1,cy+6,0xCCFFFFFF);
        graphics.drawCenteredString(font,CameraData.lens(camera)+" mm • focus "+(CameraData.focus(camera)>=1000?"infinity":CameraData.focus(camera)+" m")+" • "+(CameraData.tripod(camera,minecraft.player)==null?"handheld":"tripod"),cx,23,0xFFB7B3A5);
        String range="Near "+com.nobothehobo.candid.core.Optics.distanceLabel(focusRange.nearMeters())+" — Far "+com.nobothehobo.candid.core.Optics.distanceLabel(focusRange.farMeters());
        graphics.drawCenteredString(font,range,cx,35,0xFFD4DEC9);
        String filmText = stock == null
                ? "NO FILM • Crouch + Use for camera controls"
                : stock.displayName() + "  ISO " + stock.iso() + "  " + frames + "/36  " + (PhotoCapture.busy()?"RECORDING":wound ? "READY" : "WIND");
        graphics.drawCenteredString(font, filmText, cx, 9,
                stock == null ? 0xFFFF7070 : (wound ? 0xFFFFFFFF : 0xFFFFD060));

        String exposure = "f/" + trim(CameraData.APERTURES[apertureIndex]) + "     " + CameraData.shutterLabel(CameraData.SHUTTERS[shutterIndex]) + String.format(java.util.Locale.ROOT,"    %+.1f EV",meter);
        graphics.drawCenteredString(font, exposure, cx, height - 48, 0xFFFFFFFF);
        drawMeter(graphics, cx, height - 31, meter);
        if(Math.abs(meter)>=1)graphics.drawCenteredString(font,meter>0?"Overexposed • brighter, softer highlights":"Underexposed • darker shadows, more grain",cx,frame.y()+8,0xffebcf94);
        drawWindLever(graphics, width - 45, height - 38, wound);

        graphics.drawCenteredString(font, GamepadInput.present()
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
        windAnim = com.nobothehobo.candid.core.WindingMotion.TICKS;
        CandidClient.playLocal(CandidSounds.WIND);
    }

    private void shoot() {
        ItemStack camera = camera();
        FilmStock stock = camera.isEmpty() ? null : CameraData.film(camera);
        if (minecraft == null) return;
        if(stock==null||CameraData.frames(camera)<=0){if(minecraft.player!=null)minecraft.player.displayClientMessage(Component.literal(stock==null?"Load film before firing the shutter":"Roll finished • rewind and unload"),true);return;}
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
            case com.mojang.blaze3d.platform.InputConstants.KEY_UP -> { changeAperture(-1); yield true; }
            case com.mojang.blaze3d.platform.InputConstants.KEY_DOWN -> { changeAperture(1); yield true; }
            case com.mojang.blaze3d.platform.InputConstants.KEY_LEFT -> { changeShutter(-1); yield true; }
            case com.mojang.blaze3d.platform.InputConstants.KEY_RIGHT -> { changeShutter(1); yield true; }
            case com.mojang.blaze3d.platform.InputConstants.KEY_RETURN, com.mojang.blaze3d.platform.InputConstants.KEY_SPACE -> { shoot(); yield true; }
            case com.mojang.blaze3d.platform.InputConstants.KEY_R -> { wind(); yield true; }
            case com.mojang.blaze3d.platform.InputConstants.KEY_LBRACKET -> {CameraOptics.focus(-1);yield true;}
            case com.mojang.blaze3d.platform.InputConstants.KEY_RBRACKET -> {CameraOptics.focus(1);yield true;}
            case com.mojang.blaze3d.platform.InputConstants.KEY_F -> {focusSubject();yield true;}
            case com.mojang.blaze3d.platform.InputConstants.KEY_C -> { openControls(); yield true; }
            default -> super.keyPressed(input);
        };
    }

    private void pollGamepad() {
        if (!GamepadInput.present()) return;
        try (GamepadInput.State state = GamepadInput.read()) {
            if (!state.connected()) return;
            if(!padPrimed){for(int i=0;i<pad.length;i++)pad[i]=state.buttons(i)==1;padPrimed=true;return;}
            long now=System.nanoTime();double dt=Math.min(.05,(now-lastAim)/1e9);lastAim=now;
            if(minecraft!=null&&minecraft.player!=null){
                float ax=state.axes(GamepadInput.RIGHT_X),ay=state.axes(GamepadInput.RIGHT_Y);
                if(Math.abs(ax)>.18)CameraOptics.aim((float)(ax*70*dt),0);
                if(Math.abs(ay)>.18)CameraOptics.aim(0,(float)(ay*55*dt));
            }
            edge(state, GamepadInput.DPAD_UP, () -> changeAperture(-1));
            edge(state, GamepadInput.DPAD_DOWN, () -> changeAperture(1));
            edge(state, GamepadInput.DPAD_LEFT, () -> changeShutter(-1));
            edge(state, GamepadInput.DPAD_RIGHT, () -> changeShutter(1));
            edge(state, GamepadInput.LEFT_BUMPER, ()->CameraOptics.focus(-1));
            edge(state, GamepadInput.RIGHT_BUMPER, ()->CameraOptics.focus(1));
            edge(state, GamepadInput.LEFT_THUMB, this::focusSubject);
            edge(state, GamepadInput.A, this::shoot);
            edge(state, GamepadInput.X, this::wind);
            edge(state, GamepadInput.Y, this::openControls);
            edge(state, GamepadInput.B, this::onClose);
        }
    }

    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(vertical!=0)CameraOptics.focus(vertical>0?-1:1);return true;}
    private double lastMouseX,lastMouseY;
    @Override public void mouseMoved(double x,double y){
        if(minecraft!=null&&minecraft.player!=null&&minecraft.mouseHandler.isRightPressed()){
            CameraOptics.aim((float)((x-lastMouseX)*.22),(float)((y-lastMouseY)*.22));

        }
        lastMouseX=x;lastMouseY=y;super.mouseMoved(x,y);
    }
    private void edge(GamepadInput.State state, int button, Runnable action) {
        boolean now = state.buttons(button) == 1;
        if (now && !pad[button]) action.run();
        pad[button] = now;
    }
}
