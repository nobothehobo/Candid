package com.nobothehobo.candid.film;

public enum FilmStock {
    DAYLIGHT_100("Daylight 100", 100, 1.06f, 1.05f, 0.98f, 0.99f, 1.03f, 0.01f),
    WARM_200("Sun 200", 200, 1.04f, 1.08f, 1.05f, 1.00f, 0.94f, 0.018f),
    PORTRAIT_400("Portrait 400", 400, 0.97f, 0.98f, 1.04f, 1.01f, 0.98f, 0.025f),
    NIGHT_800("Night 800", 800, 1.09f, 0.94f, 1.03f, 0.99f, 0.96f, 0.055f),
    MONO_400("Mono 400", 400, 1.12f, 0.0f, 1.0f, 1.0f, 1.0f, 0.04f);

    private final String displayName;
    private final int iso;
    private final float contrast;
    private final float saturation;
    private final float red;
    private final float green;
    private final float blue;
    private final float grain;

    FilmStock(String displayName, int iso, float contrast, float saturation, float red, float green, float blue, float grain) {
        this.displayName = displayName;
        this.iso = iso;
        this.contrast = contrast;
        this.saturation = saturation;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.grain = grain;
    }

    public String displayName() { return displayName; }
    public int iso() { return iso; }
    public float contrast() { return contrast; }
    public float saturation() { return saturation; }
    public float red() { return red; }
    public float green() { return green; }
    public float blue() { return blue; }
    public float grain() { return grain; }

    public static FilmStock byName(String name) {
        try { return valueOf(name); } catch (Exception ignored) { return null; }
    }
}
