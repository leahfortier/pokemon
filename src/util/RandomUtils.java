package util;

import main.Global;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

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

    // Returns a random value in values, where the probability of each value is specified in the chanceMapper
    // It is expected that the sum of all values put through the mapper is exactly 100
    public static <T> T getPercentageValue(List<T> values, Function<T, Integer> chanceMapper) {
        int[] chances = new int[values.size()];
        int sum = 0;
        for (int i = 0; i < values.size(); i++) {
            chances[i] = chanceMapper.apply(values.get(i));
            sum += chances[i];
        }

        if (sum != 100) {
            Global.error("Chances array is improperly formatted.");
        }

        return values.get(getPercentageIndex(chances));
    }

    // Returns a random index in chances, where each index has the probability of chance[index]
    // It is expected that the sum of chances is exactly 100
    public static int getPercentageIndex(int[] chances) {
        int sum = 0;
        int random = getRandomInt(100);

        for (int i = 0; i < chances.length; i++) {
            sum += chances[i];
            if (random < sum) {
                return i;
            }
        }

        Global.error("Chances array is improperly formatted.");
        return -1;
    }
}
