package com.nobothehobo.candid.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.nobothehobo.candid.data.CameraData;
import com.nobothehobo.candid.film.FilmStock;
import com.nobothehobo.candid.network.CapturePhotoPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;

import java.util.Random;

public final class PhotoCapture {
    private static final int[] PALETTE = buildPalette();
    private static Pending pending;
    private static int waitTicks;

    private PhotoCapture() { }

    public static void queue(ItemStack camera, int apertureIndex, int shutterIndex, float meterStops) {
        FilmStock stock = CameraData.film(camera);
        if (stock == null || CameraData.frames(camera) <= 0 || !CameraData.isWound(camera)) return;
        pending = new Pending(stock, apertureIndex, shutterIndex, meterStops);
        waitTicks = 2;
    }

    public static void tick(Minecraft client) {
        if (pending == null || client.level == null || client.player == null) return;
        if (waitTicks-- > 0) return;
        Pending shot = pending;
        pending = null;

        Screenshot.takeScreenshot(client.getMainRenderTarget(), image -> {
            try {
                byte[] colors = convert(image, shot);
                client.execute(() -> {
                    if (ClientPlayNetworking.canSend(CapturePhotoPayload.ID)) {
                        ClientPlayNetworking.send(new CapturePhotoPayload(colors, shot.apertureIndex, shot.shutterIndex));
                    }
                    if (client.player != null) client.setScreen(new CameraScreen());
                });
            } finally {
                image.close();
            }
        });
    }

    private static byte[] convert(NativeImage image, Pending shot) {
        byte[] out = new byte[128 * 128];
        int width = image.getWidth();
        int height = image.getHeight();

        float rawStops = Math.max(-5.0f, Math.min(5.0f, shot.meterStops));
        float response = rawStops < 0 ? shot.stock.underResponse() : shot.stock.overResponse();
        float effectiveStops = rawStops * response;
        double exposure = Math.pow(2.0, effectiveStops);

        // Deliberately deterministic per capture dimensions so map photos remain stable.
        Random random = new Random(0xCA4D1DL + width * 31L + height * 17L + shot.stock.ordinal() * 101L);

        for (int y = 0; y < 128; y++) {
            int sy = Math.min(height - 1, (int) (((127 - y) + 0.5) * height / 128.0));
            for (int x = 0; x < 128; x++) {
                int sx = Math.min(width - 1, (int) ((x + 0.5) * width / 128.0));
                int argb = image.getPixel(sx, sy);

                float r = ARGB.red(argb) / 255f;
                float g = ARGB.green(argb) / 255f;
                float b = ARGB.blue(argb) / 255f;

                // Exposure first, then a negative-film-like shoulder instead of hard digital clipping.
                r = filmShoulder((float) (r * exposure), shot.stock.overResponse());
                g = filmShoulder((float) (g * exposure), shot.stock.overResponse());
                b = filmShoulder((float) (b * exposure), shot.stock.overResponse());

                // Underexposure progressively loses shadow separation. Faster portrait film is intentionally
                // more forgiving because its underResponse is lower.
                if (rawStops < 0) {
                    float severity = Math.min(1.6f, -rawStops / 3.0f);
                    float toe = 1.0f + (shot.stock.shadowToe() - 1.0f) * severity;
                    r = (float) Math.pow(clamp(r), toe);
                    g = (float) Math.pow(clamp(g), toe);
                    b = (float) Math.pow(clamp(b), toe);

                    // Mild color-neg underexposure contamination: consumer stocks go a little cool/green,
                    // portrait stocks remain more neutral.
                    if (!shot.stock.monochrome()) {
                        float consumerShift = Math.max(0f, shot.stock.saturation() - 0.97f);
                        r *= 1.0f - severity * (0.025f + consumerShift * 0.10f);
                        g *= 1.0f + severity * 0.015f;
                        b *= 1.0f + severity * (0.018f + consumerShift * 0.07f);
                    }
                }

                float luma = r * 0.2126f + g * 0.7152f + b * 0.0722f;
                r = luma + (r - luma) * shot.stock.saturation();
                g = luma + (g - luma) * shot.stock.saturation();
                b = luma + (b - luma) * shot.stock.saturation();

                r = (r - 0.5f) * shot.stock.contrast() + 0.5f;
                g = (g - 0.5f) * shot.stock.contrast() + 0.5f;
                b = (b - 0.5f) * shot.stock.contrast() + 0.5f;

                r *= shot.stock.red();
                g *= shot.stock.green();
                b *= shot.stock.blue();

                if (shot.stock.monochrome()) {
                    // Panchromatic weighting: green contributes most, then red, then blue.
                    float mono = r * 0.30f + g * 0.59f + b * 0.11f;
                    r = g = b = mono;
                }

                // Grain grows in underexposure and in darker parts of the frame, as real scans tend to show.
                float finalLuma = clamp(r * 0.2126f + g * 0.7152f + b * 0.0722f);
                float shadowGrain = 0.70f + (1.0f - finalLuma) * 0.75f;
                float underGrain = 1.0f + Math.max(0f, -rawStops) * 0.14f;
                float grainAmp = shot.stock.grain() * shadowGrain * underGrain;

                float monoNoise = (random.nextFloat() - 0.5f) * grainAmp;
                float chroma = shot.stock.chromaGrain() * grainAmp;
                float rNoise = monoNoise + (random.nextFloat() - 0.5f) * chroma;
                float gNoise = monoNoise + (random.nextFloat() - 0.5f) * chroma * 0.70f;
                float bNoise = monoNoise + (random.nextFloat() - 0.5f) * chroma * 0.90f;

                r = clamp(r + rNoise);
                g = clamp(g + gNoise);
                b = clamp(b + bNoise);

                out[y * 128 + x] = (byte) nearest(
                        Math.round(r * 255),
                        Math.round(g * 255),
                        Math.round(b * 255)
                );
            }
        }
        return out;
    }

    private static float filmShoulder(float value, float overResponse) {
        value = Math.max(0f, value);
        if (value <= 0.72f) return value;

        float shoulderStrength = 1.25f + (1.0f - overResponse) * 2.0f;
        float t = (value - 0.72f) / 0.28f;
        float compressed = 1.0f - (float) Math.exp(-t / shoulderStrength);
        return 0.72f + 0.28f * compressed;
    }

    private static float clamp(float value) { return Math.max(0f, Math.min(1f, value)); }

    private static int[] buildPalette() {
        int[] palette = new int[256];
        for (int i = 0; i < palette.length; i++) palette[i] = MapColor.getColorFromPackedId(i);
        return palette;
    }

    private static int nearest(int r, int g, int b) {
        int best = 4;
        long bestDistance = Long.MAX_VALUE;
        for (int i = 4; i < PALETTE.length; i++) {
            int c = PALETTE[i];
            int cr = (c >> 16) & 255;
            int cg = (c >> 8) & 255;
            int cb = c & 255;
            long dr = r - cr, dg = g - cg, db = b - cb;
            long d = dr * dr + dg * dg + db * db;
            if (d < bestDistance) { bestDistance = d; best = i; }
        }
        return best;
    }

    private record Pending(FilmStock stock, int apertureIndex, int shutterIndex, float meterStops) { }
}
