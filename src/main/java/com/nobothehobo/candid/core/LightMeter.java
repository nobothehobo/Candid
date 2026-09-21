package com.nobothehobo.candid.core;

import java.util.List;

public final class LightMeter {
  public record Sample(int sky, int block, double weight) {}

  private LightMeter() {}

  public static double daylight(long time, boolean rain, boolean thunder) {
    double sun = Math.max(0, Math.sin((time % 24000 + 0.0) / 12000 * Math.PI));
    return (0.0002 + .9998 * Math.pow(sun, .65)) * (thunder ? .18 : rain ? .45 : 1);
  }

  /** Average linear scene luminance, then convert to calibrated EV; full noon skylight = EV15. */
  public static double estimate(
      List<Sample> samples, double daylight, boolean hasSky, double calibration) {
    if (samples.isEmpty()) throw new IllegalArgumentException("No meter samples");
    double total = 0, weights = 0;
    for (Sample s : samples) {
      double sky = hasSky ? daylight * Math.pow(Math.max(0, Math.min(15, s.sky)) / 15.0, 3) : 0;
      double block = .06 * Math.pow(Math.max(0, Math.min(15, s.block)) / 15.0, 3);
      total += (.00002 + sky + block) * s.weight;
      weights += s.weight;
    }
    if (weights <= 0) throw new IllegalArgumentException("Invalid weights");
    return Math.max(-3, Math.min(17, 15 + Exposure.log2(total / weights) + calibration));
  }
}
