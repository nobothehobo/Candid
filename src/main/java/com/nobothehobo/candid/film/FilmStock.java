package com.nobothehobo.candid.film;

public enum FilmStock {
    // Fine-grain, vivid 100-speed color negative: outdoor/travel oriented.
    DAYLIGHT_100("Vivid 100", 100,
            1.10f, 1.14f, 1.02f, 1.00f, 0.98f,
            0.010f, 0.18f, 1.15f, 0.72f, 1.20f, false),

    // Gold-like consumer 200: warm, saturated, fine grain, forgiving highlights.
    WARM_200("Golden 200", 200,
            1.025f, 1.035f, 1.035f, 1.0f, 0.975f,
            0.012f, 0.16f, 1.00f, 0.62f, 1.08f, false),

    // UltraMax-like general-purpose 400: punchier than portrait film with useful shadow latitude.
    EVERYDAY_400("Everyday 400", 400,
            1.08f, 1.09f, 1.03f, 1.01f, 0.97f,
            0.030f, 0.34f, 0.93f, 0.58f, 1.12f, false),

    // Portra-like 400: soft shoulder, natural color, restrained saturation and strong latitude.
    PORTRAIT_400("Portrait 400", 400,
            0.96f, 0.96f, 1.025f, 1.005f, 0.985f,
            0.022f, 0.25f, 0.86f, 0.48f, 0.96f, false),

    // Portra-like 800: high speed with better underexposure protection than the consumer stocks.
    NIGHT_800("Portrait 800", 800,
            1.00f, 0.98f, 1.03f, 1.01f, 0.98f,
            0.042f, 0.38f, 0.77f, 0.52f, 1.00f, false),

    // Tri-X-like 400: classic, visible grain and stronger midtone bite.
    MONO_400("Classic Mono 400", 400,
            1.14f, 0.0f, 1.0f, 1.0f, 1.0f,
            0.048f, 0.06f, 0.90f, 0.58f, 1.18f, true),

    // T-Max-like 400: finer grain, cleaner edges and a smoother tonal scale.
    FINE_MONO_400("Fine Mono 400", 400,
            1.06f, 0.0f, 1.0f, 1.0f, 1.0f,
            0.024f, 0.03f, 0.84f, 0.50f, 1.06f, true);

    private final String displayName;
    private final int iso;
    private final float contrast;
    private final float saturation;
    private final float red;
    private final float green;
    private final float blue;
    private final float grain;
    private final float chromaGrain;
    private final float underResponse;
    private final float overResponse;
    private final float shadowToe;
    private final boolean monochrome;

    FilmStock(String displayName, int iso, float contrast, float saturation,
              float red, float green, float blue, float grain, float chromaGrain,
              float underResponse, float overResponse, float shadowToe, boolean monochrome) {
        this.displayName = displayName;
        this.iso = iso;
        this.contrast = contrast;
        this.saturation = saturation;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.grain = grain;
        this.chromaGrain = chromaGrain;
        this.underResponse = underResponse;
        this.overResponse = overResponse;
        this.shadowToe = shadowToe;
        this.monochrome = monochrome;
    }

    public String displayName() { return displayName; }
    public int iso() { return iso; }
    public float contrast() { return contrast; }
    public float saturation() { return saturation; }
    public float red() { return red; }
    public float green() { return green; }
    public float blue() { return blue; }
    public float grain() { return grain; }
    public float chromaGrain() { return chromaGrain; }
    public float underResponse() { return underResponse; }
    public float overResponse() { return overResponse; }
    public float shadowToe() { return shadowToe; }
    public boolean monochrome() { return monochrome; }

    public static FilmStock byName(String name) {
        try { return valueOf(name); } catch (Exception ignored) { return null; }
    }
}
