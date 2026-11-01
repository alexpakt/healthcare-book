package com.healthcare.childrecords.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrowthPercentilesTest {

    // The normal CDF uses a Chebyshev erfc approximation (error about 1e-7), so compare with 1e-4 for percentiles.
    private static final double TOLERANCE = 1e-6;
    private static final double PERCENTILE_TOLERANCE = 1e-4;

    @Test
    void medianHasZScoreZeroAndPercentileFifty() {
        assertEquals(0.0, GrowthPercentiles.zScore(50.0, 1.0, 50.0, 0.1), TOLERANCE);
        assertEquals(50.0, GrowthPercentiles.percentile(50.0, 1.0, 50.0, 0.1), PERCENTILE_TOLERANCE);
    }

    @Test
    void zScoreWithPowerL() {
        // (55/50 - 1) / (1 * 0.1) = 1
        assertEquals(1.0, GrowthPercentiles.zScore(55.0, 1.0, 50.0, 0.1), TOLERANCE);
    }

    @Test
    void zScoreWithLZero() {
        // ln(X/M) / S with X = M * exp(S)
        assertEquals(1.0, GrowthPercentiles.zScore(50.0 * Math.exp(0.1), 0.0, 50.0, 0.1), TOLERANCE);
    }

    @Test
    void normalCdfMatchesKnownValues() {
        assertEquals(0.5, GrowthPercentiles.normalCdf(0.0), TOLERANCE);
        assertEquals(0.9750021, GrowthPercentiles.normalCdf(1.96), 1e-6);
        assertEquals(0.1586553, GrowthPercentiles.normalCdf(-1.0), 1e-6);
    }

    @Test
    void oneStandardDeviationAboveIsAboutEightyFourthPercentile() {
        assertEquals(84.13, GrowthPercentiles.percentile(55.0, 1.0, 50.0, 0.1), 0.01);
    }
}
