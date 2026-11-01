package com.healthcare.childrecords.util;

/**
 * WHO LMS method: a measurement X becomes a z-score with the curve's L (power), M (median) and S (coefficient of variation),
 * and the z-score becomes a percentile through the standard normal cumulative distribution function.
 */
public final class GrowthPercentiles {

    private GrowthPercentiles() {
    }

    /** z = ((X/M)^L - 1) / (L*S), or ln(X/M) / S when L = 0. */
    public static double zScore(double x, double l, double m, double s) {
        if (l == 0) {
            return Math.log(x / m) / s;
        }
        return (Math.pow(x / m, l) - 1) / (l * s);
    }

    /** Percentile in the range 0..100: 100 * Phi(z). */
    public static double percentile(double x, double l, double m, double s) {
        return 100 * normalCdf(zScore(x, l, m, s));
    }

    /** Percentile rounded to the nearest integer. */
    public static int roundedPercentile(double x, double l, double m, double s) {
        return (int) Math.round(percentile(x, l, m, s));
    }

    /** Standard normal cumulative distribution function. */
    public static double normalCdf(double z) {
        return 0.5 * erfc(-z / Math.sqrt(2));
    }

    // Complementary error function, Chebyshev approximation (fractional error below 1.2e-7).
    private static double erfc(double x) {
        double t = 1 / (1 + 0.5 * Math.abs(x));
        double ans = t * Math.exp(-x * x - 1.26551223 + t * (1.00002368 + t * (0.37409196
                + t * (0.09678418 + t * (-0.18628806 + t * (0.27886807 + t * (-1.13520398
                + t * (1.48851587 + t * (-0.82215223 + t * 0.17087277)))))))));
        return x >= 0 ? ans : 2 - ans;
    }
}
