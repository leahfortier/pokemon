package test.general;

import org.junit.Assert;

import java.util.Arrays;
import java.util.function.Function;

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

    public static void assertGreater(boolean firstGreater, double first, double second) {
        assertGreater(firstGreater ? first : second, firstGreater ? second : first);
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

    public static void assertPositive(String message, double value) {
        assertGreater(message, value, 0);
    }

    public static void assertInclusiveRange(String message, double expectedLower, double expectedUpper, double actual) {
        Assert.assertTrue(
                message + " !(" + expectedLower + " <= " + actual + " <= " + expectedUpper + ")",
                expectedLower <= actual && actual <= expectedUpper
        );
    }

    public static void assertExclusiveRange(String message, double expectedLower, double expectedUpper, double actual) {
        Assert.assertTrue(
                message + " !(" + expectedLower + " <= " + actual + " <= " + expectedUpper + ")",
                expectedLower <= actual && actual <= expectedUpper
        );
    }

    // Confirms that the actual value is NOT between lower and upper inclusive
    public static void assertOutsideRange(double lower, double upper, double actual) {
        Assert.assertTrue(
                lower + " <= " + actual + " <= " + upper,
                actual < lower || actual > upper
        );
    }

    // Asserts that the description matches the given regex
    // Ex: "[A-Z][a-zA-Z0-9.,'/:é°\"\\- ]+[.!]" for starting with capital letter, ending with either period or
    // exclamation, and all middle characters are valid
    // Also I really don't like when periods come before the quotation they should be after...
    // Double-hyphens must be surrounded by spaces
    // Single-hyphens must NOT be surrounded by spaces (jk there are some appropriate cases for post space)
    // 20 is kind of arbitrary (and kind of short) but just to make sure it's something
    public static void assertDescription(String name, String description, String regex) {
        String message = name + " " + description;
        Assert.assertTrue(message, description.matches(regex));
        Assert.assertFalse(message, description.contains(".\""));
        Assert.assertFalse(message, description.contains("[^ ]--[^ ]"));
        Assert.assertFalse(message, description.contains(" -[^-]"));
        Assert.assertFalse(message, description.contains(" the the "));
        TestUtils.assertGreater(message, description.length(), 20);
    }

    // If does not pass the assertion, prints the message to standard error instead of failing test entirely
    public static void assertWarning(String message, boolean assertion) {
        try {
            Assert.assertTrue(message, assertion);
        } catch (AssertionError error) {
            System.err.println(error.getMessage());
        }
    }

    public static <T> void assertEqualProperty(String message, T first, T second, Function<T, ?> getProperty) {
        Assert.assertEquals(message, getProperty.apply(first), getProperty.apply(second));
    }
}
