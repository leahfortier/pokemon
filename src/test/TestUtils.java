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

    public static void semiAssertTrue(String message, boolean assertion) {
        semiAssertTrue(message, false, assertion);
    }

    public static void semiAssertTrue(String message, boolean fullAssert, boolean assertion) {
        if (!assertion) {
            if (fullAssert) {
                Assert.fail(message);
            } else {
                System.err.println(message);
            }
        }
    }

    public static void assertGreater(String message, double greater, double lesser) {
        Assert.assertTrue(
                message + " " + greater + " !> " + lesser,
                greater > lesser
        );
    }
}
