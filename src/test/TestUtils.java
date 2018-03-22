package test;

import org.junit.Assert;

public class TestUtils {
    private static final double DELTA = 1e-15;

    public static void assertEquals(String message, double expected, double actual) {
        Assert.assertEquals(message, expected, actual, DELTA);
    }

    public static void assertEquals(double expected, double actual) {
        Assert.assertEquals(expected, actual, DELTA);
    }

    // Close enough
    public static void assertAlmostEquals(String message, int expected, int actual, int delta) {
        if (Math.abs(expected - actual) > delta) {
            Assert.fail(message + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    public static void assertGreater(String message, double greater, double lesser) {
        Assert.assertTrue(
                message + " " + greater + " !> " + lesser,
                greater > lesser
        );
    }
}
