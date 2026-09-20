package com.nobothehobo.candid.core;

public final class Exposure {
  private Exposure() {}

  public static final double[] APERTURES = {2, 2.8, 4, 5.6, 8, 11, 16};
  public static final int[] SHUTTERS = {15, 30, 60, 125, 250, 500, 1000};

  public record Settings(double aperture, int denominator) {
    public Settings {
      if (!Double.isFinite(aperture)
          || aperture < 1
          || aperture > 64
          || denominator < 1
          || denominator > 16000) throw new IllegalArgumentException("Invalid exposure settings");
    }

    public String label() {
      return "f/" + format(aperture) + " • 1/" + denominator;
    }
  }

  public static double log2(double x) {
    return Math.log(x) / Math.log(2);
  }

  public static double ev(Settings s) {
    return log2(s.aperture * s.aperture * s.denominator);
  }

  /** Positive means MORE exposure than metered, not a recommendation to add exposure. */
  public static double offset(double sceneEv100, Settings s, int iso) {
    if (!Double.isFinite(sceneEv100) || iso < 1)
      throw new IllegalArgumentException("Invalid meter/ISO");
    return sceneEv100 + log2(iso / 100.0) - ev(s);
  }

  public static String format(double n) {
    return n == Math.rint(n) ? Integer.toString((int) n) : Double.toString(n);
  }
}
