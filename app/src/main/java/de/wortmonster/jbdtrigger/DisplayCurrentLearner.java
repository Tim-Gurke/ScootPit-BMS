package de.wortmonster.jbdtrigger;

import java.util.ArrayDeque;

/** Learns a conservative per-scooter powered-at-rest current from stable BMS samples. */
final class DisplayCurrentLearner {
    private static final int REQUIRED_SAMPLES = 12;
    private static final double MAX_LEARNABLE_AMPS = 2.0;
    private final ArrayDeque<Double> samples = new ArrayDeque<>();
    private double learnedAmps;
    private double learnedResolution;
    private double sampleResolution;

    DisplayCurrentLearner(double learnedAmps, double learnedResolution) {
        if (Double.isFinite(learnedAmps) && learnedAmps > 0
                && Double.isFinite(learnedResolution) && learnedResolution > 0) {
            this.learnedAmps = learnedAmps;
            this.learnedResolution = learnedResolution;
        }
    }

    boolean observe(double signedCurrent, double resolution, boolean eligible) {
        if (!eligible || !Double.isFinite(signedCurrent) || signedCurrent >= 0
                || !Double.isFinite(resolution) || resolution <= 0
                || -signedCurrent < resolution / 2.0 || -signedCurrent > MAX_LEARNABLE_AMPS) {
            samples.clear();
            sampleResolution = 0;
            return false;
        }
        double amps = -signedCurrent;
        if (!samples.isEmpty() && (Math.abs(resolution - sampleResolution) > 1e-9
                || max(samples, amps) - min(samples, amps) > Math.max(0.03, resolution * 2.0))) {
            samples.clear();
        }
        if (samples.isEmpty()) sampleResolution = resolution;
        samples.addLast(amps);
        while (samples.size() > REQUIRED_SAMPLES) samples.removeFirst();
        if (samples.size() < REQUIRED_SAMPLES) return false;

        // Keep the lowest stable powered-at-rest observation. This errs toward waiting longer
        // before allowing a cutoff when the scooter's standing load varies.
        double candidate = min(samples, Double.POSITIVE_INFINITY);
        if (learnedAmps > 0 && candidate >= learnedAmps) return false;
        learnedAmps = candidate;
        learnedResolution = sampleResolution;
        return true;
    }

    double learnedAmps() { return learnedAmps; }
    double learnedResolution() { return learnedResolution; }
    void resetSamples() { samples.clear(); sampleResolution = 0; }

    /** Require a full measurement step below the learned powered-at-rest baseline. */
    static boolean safelyBelow(double signedCurrent, double currentResolution,
                               double learnedAmps, double learnedResolution) {
        if (!Double.isFinite(signedCurrent) || !Double.isFinite(currentResolution)
                || !Double.isFinite(learnedAmps) || !Double.isFinite(learnedResolution)
                || currentResolution <= 0 || learnedResolution <= 0 || learnedAmps <= 0) return false;
        double baselineLowerBound = learnedAmps - learnedResolution / 2.0;
        return baselineLowerBound > 0
                && Math.abs(signedCurrent) + Math.max(currentResolution, learnedResolution / 2.0)
                < baselineLowerBound;
    }

    private static double min(ArrayDeque<Double> values, double value) {
        for (double sample : values) value = Math.min(value, sample);
        return value;
    }
    private static double max(ArrayDeque<Double> values, double value) {
        for (double sample : values) value = Math.max(value, sample);
        return value;
    }
}
