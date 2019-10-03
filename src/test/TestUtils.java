package test;

import org.junit.Assert;

import java.util.Arrays;

public class TestUtils {
    private static final double DELTA = 1e-15;

    public static void assertEquals(String message, double expected, double actual) {
        Assert.assertEquals(message, expected, actual, DELTA);
    }

    public static void assertEquals(double expected, double actual) {
        Assert.assertEquals(expected, actual, DELTA);
    }

    public static void assertEquals(String message, int[] expected, int[] actual) {
        Assert.assertArrayEquals(
                message + "\n" + Arrays.toString(expected) + " " + Arrays.toString(actual),
                expected, actual
        );
        Assert.assertEquals(message, Arrays.toString(expected), Arrays.toString(actual));
    }

    public static void assertEqualsAny(String message, int actual, int... expectedOptions) {
        for (int expected : expectedOptions) {
            if (actual == expected) {
                return;
            }
        }
        Assert.fail(message + "\n" + actual + " not in " + Arrays.toString(expectedOptions));
    }

    public static void assertEqualsAny(String message, String actual, String... expectedOptions) {
        for (String expected : expectedOptions) {
            if (actual.equals(expected)) {
                return;
            }
        }
        Assert.fail(message + "\n" + actual + " not in " + Arrays.toString(expectedOptions));
    }

    // Close enough
    public static void assertAlmostEquals(String message, int expected, int actual, int delta) {
        if (Math.abs(expected - actual) > delta) {
            Assert.fail(message + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    public static void assertGreater(double greater, double lesser) {
        assertGreater("", greater, lesser);
    }

    public static void assertGreater(String message, double greater, double lesser) {
        Assert.assertTrue(
                message + " " + greater + " !> " + lesser,
                greater > lesser
        );
    }

    public static void assertInclusiveRange(String message, double expectedLower, double expectedUpper, double actual) {
        Assert.assertTrue(
                message + " !(" + expectedLower + " <= " + actual + " <= " + expectedUpper + ")",
                expectedLower <= actual && actual <= expectedUpper
        );
    }

    public static void assertUnique(int[] values) {
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                Assert.assertNotEquals(Arrays.toString(values), values[i], values[j]);
            }
        }
    }
}
