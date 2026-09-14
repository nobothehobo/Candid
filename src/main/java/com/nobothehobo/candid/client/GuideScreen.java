package com.nobothehobo.candid.client;

import com.nobothehobo.candid.content.CandidBlocks;
import com.nobothehobo.candid.content.CandidItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

public class GuideScreen extends Screen {
    private int page;
    private final boolean[] pad = new boolean[15];
    private static final int PAGES = 6;

    public GuideScreen() { super(Component.literal("Candid Field Guide")); }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("‹"), b -> previous()).bounds(width / 2 - 92, height - 34, 40, 20).build());
        addRenderableWidget(Button.builder(Component.literal("›"), b -> next()).bounds(width / 2 + 52, height - 34, 40, 20).build());
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        pollGamepad();
        g.fill(0, 0, width, height, 0xE1141210);
        g.fill(width / 2 - 130, 18, width / 2 + 130, height - 48, 0xFFF0E4C8);
        g.submitOutline(width / 2 - 130, 18, 260, height - 66, 0xFF5A4934);
        g.drawCenteredString(font, "CANDID • 35mm FIELD GUIDE", width / 2, 28, 0xFF3A2B1C);
        g.drawCenteredString(font, (page + 1) + " / " + PAGES, width / 2, height - 29, 0xFF8A765D);
        renderPage(g);
        super.render(g, mouseX, mouseY, delta);
    }

    private void renderPage(GuiGraphics g) {
        int x = width / 2 - 112;
        int y = 50;
        switch (page) {
            case 0 -> {
                heading(g, "1. The Candid 35 Camera", y);
                drawRecipe(g, x + 28, y + 28,
                        new Item[][]{{Items.IRON_INGOT, Items.GLASS_PANE, Items.IRON_INGOT}, {Items.COPPER_INGOT, Items.REDSTONE, Items.COPPER_INGOT}, {Items.IRON_INGOT, Items.GLASS_PANE, Items.IRON_INGOT}},
                        CandidItems.CAMERA);
                text(g, "Use: open the viewfinder. Crouch + Use: open the camera body controls, film door, and physical dials. Each roll has 36 exposures.", x, y + 96);
                text(g, "The meter reads scene brightness. Adjust aperture and shutter until the needle is near 0.", x, y + 126);
            }
            case 1 -> {
                heading(g, "2. Film Stocks", y);
                iconRow(g, x, y + 28, CandidItems.FILM_DAYLIGHT_100, CandidItems.FILM_SUN_200, CandidItems.FILM_PORTRAIT_400, CandidItems.FILM_NIGHT_800, CandidItems.FILM_MONO_400);
                text(g, "Daylight 100: clean, fine grain. Sun 200: warm consumer color. Portrait 400: softer contrast and skin-friendly warmth.", x, y + 62);
                text(g, "Night 800: faster, grainier low-light color. Mono 400: classic black-and-white.", x, y + 105);
                text(g, "Higher ISO needs less light but adds more grain.", x, y + 137);
            }
            case 2 -> {
                heading(g, "3. Exposure Controls", y);
                text(g, "Aperture: f/1.4 → f/16. Smaller f-number = more light. Shutter: 1/15 → 1/1000. Slower shutter = more light.", x, y + 30);
                text(g, "Steam Deck: D-pad adjusts exposure, A fires, X manually winds, Y opens body controls, B exits. Keyboard: arrows adjust, Enter/Space fires, R winds, C opens controls.", x, y + 83);
                text(g, "Meter left = underexposed. Meter right = overexposed. Center it for a normal negative, or expose creatively.", x, y + 126);
            }
            case 3 -> {
                heading(g, "4. Darkroom Basin", y);
                drawRecipe(g, x + 28, y + 28,
                        new Item[][]{{Items.IRON_INGOT, Items.CAULDRON, Items.IRON_INGOT}, {Items.REDSTONE, Items.GLASS_BOTTLE, Items.REDSTONE}, {Items.IRON_INGOT, Items.IRON_INGOT, Items.IRON_INGOT}},
                        CandidBlocks.DARKROOM_BASIN.asItem());
                text(g, "A shutter press gives an exposed negative map. Hold it and use the Darkroom Basin with Developer Chemistry in your inventory.", x, y + 96);
                iconRow(g, x + 70, y + 128, CandidItems.DEVELOPER);
            }
            case 4 -> {
                heading(g, "5. Enlarger & Extra Prints", y);
                drawRecipe(g, x + 28, y + 28,
                        new Item[][]{{Items.IRON_INGOT, Items.REDSTONE_LAMP, Items.IRON_INGOT}, {Items.IRON_INGOT, Items.GLASS_PANE, Items.IRON_INGOT}, {Items.IRON_INGOT, Items.SMOOTH_STONE, Items.IRON_INGOT}},
                        CandidBlocks.ENLARGER.asItem());
                text(g, "Use a developed print on the Enlarger while carrying Candid Photo Paper to make a duplicate.", x, y + 96);
                text(g, "Prints are vanilla map items: hold them, put them in item frames, and build gallery walls.", x, y + 131);
            }
            case 5 -> {
                heading(g, "6. Quick Workflow", y);
                text(g, "1  Craft camera + film.   2  Crouch + Use, select FILM, press A and watch the back-loading sequence.   3  Use camera to enter viewfinder.", x, y + 30);
                text(g, "4  Meter and expose with A.   5  Press X to wind before the next frame.   6  Develop exposed negatives in the basin.", x, y + 80);
                text(g, "7  Use the Enlarger + Photo Paper for duplicates. Film looks are original Candid profiles inspired by classic color-negative photography.", x, y + 130);
            }
        }
    }

    private void heading(GuiGraphics g, String s, int y) {
        g.drawCenteredString(font, s, width / 2, y, 0xFF3A2B1C);
    }

    private void text(GuiGraphics g, String s, int x, int y) {
        int lineY = y;
        for (FormattedCharSequence line : font.split(Component.literal(s), 220)) {
            g.drawString(font, line, x, lineY, 0xFF4B3C2C, false);
            lineY += 11;
        }
    }

    private void iconRow(GuiGraphics g, int x, int y, Item... items) {
        int offset = 0;
        for (Item item : items) {
            g.renderItem(new ItemStack(item), x + offset, y);
            offset += 34;
        }
    }

    private void drawRecipe(GuiGraphics g, int x, int y, Item[][] grid, Item result) {
        for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++) {
            int px = x + col * 20;
            int py = y + row * 20;
            g.fill(px - 1, py - 1, px + 18, py + 18, 0x664B3C2C);
            Item ingredient = grid[row][col];
            if (ingredient != null) g.renderItem(new ItemStack(ingredient), px, py);
        }
        g.drawString(font, "→", x + 67, y + 22, 0xFF3A2B1C, false);
        g.renderItem(new ItemStack(result), x + 84, y + 20);
    }

    private void previous() { page = Math.floorMod(page - 1, PAGES); }
    private void next() { page = Math.floorMod(page + 1, PAGES); }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == GLFW.GLFW_KEY_LEFT) { previous(); return true; }
        if (input.key() == GLFW.GLFW_KEY_RIGHT) { next(); return true; }
        return super.keyPressed(input);
    }

    private void pollGamepad() {
        if (!GLFW.glfwJoystickIsGamepad(GLFW.GLFW_JOYSTICK_1)) return;
        try (GLFWGamepadState state = GLFWGamepadState.calloc()) {
            if (!GLFW.glfwGetGamepadState(GLFW.GLFW_JOYSTICK_1, state)) return;
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER, this::previous);
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER, this::next);
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT, this::previous);
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT, this::next);
            edge(state, GLFW.GLFW_GAMEPAD_BUTTON_B, this::onClose);
        }
    }

    private void edge(GLFWGamepadState state, int button, Runnable action) {
        boolean now = state.buttons(button) == GLFW.GLFW_PRESS;
        if (now && !pad[button]) action.run();
        pad[button] = now;
    }
}
