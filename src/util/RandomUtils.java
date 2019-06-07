package util;

import main.Global;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

// Util class with methods related to RNG
public final class RandomUtils {
    private static final Random RANDOM = new Random();
    private static final long SEED = RANDOM.nextLong();

    static {
        RANDOM.setSeed(SEED);
    }

    // Utility class -- should not be instantiated
    private RandomUtils() {
        Global.error(this.getClass().getSimpleName() + " class cannot be instantiated.");
    }

    public static long getSeed() {
        return SEED;
    }

    public static void setTempRandomSeed(long tempRandomSeed) {
        RANDOM.setSeed(tempRandomSeed);
    }

    public static void resetRandomSeedToInitial() {
        RANDOM.setSeed(SEED);
    }

    public static boolean chanceTest(final int chance) {
        return chanceTest(chance, 100);
    }

    public static boolean chanceTest(final int numerator, final int denominator) {
        return getRandomInt(denominator) < numerator;
    }

    // Returns a random int with exclusive upper bound from range [0, upperBound)
    public static int getRandomInt(final int upperBound) {
        if (upperBound <= 0) {
            return 0;
        }

        return RANDOM.nextInt(upperBound);
    }

    // Returns a random int from the inclusive range [lowerBound, upperBound]
    public static int getRandomInt(final int lowerBound, final int upperBound) {
        if (upperBound < lowerBound) {
            Global.error("Upper bound should never be lower than the lower bound. " +
                                 "(Lower: " + lowerBound + ", Upper: " + upperBound + ")");
        }

        return getRandomInt((upperBound - lowerBound + 1)) + lowerBound;
    }

    public static <T> T getRandomValue(T[] array) {
        return getRandomValue(Arrays.asList(array));
    }

    public static <T> T getRandomValue(List<T> list) {
        return list.get(getRandomIndex(list));
    }

    public static <T> int getRandomIndex(T[] array) {
        return getRandomIndex(Arrays.asList(array));
    }

    public static <T> int getRandomIndex(List<T> list) {
        return getRandomInt(list.size());
    }
}
