package util;

import main.Global;
import util.string.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GeneralUtils {

    // Utility class -- should not be instantiated
    private GeneralUtils() {
        Global.error(this.getClass().getSimpleName() + " class cannot be instantiated.");
    }

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

    public static int getPercentageIndex(int[] chances) {
        int sum = 0;
        int random = RandomUtils.getRandomInt(100);

        for (int i = 0; i < chances.length; i++) {
            sum += chances[i];
            if (random < sum) {
                return i;
            }
        }

        Global.error("Chances array is improperly formatted.");
        return -1;
    }

    public static void shiftArray(int[] array, int shift) {
        int[] temp = array.clone();
        for (int i = 0; i < array.length; i++) {
            array[i] = temp[(i + shift)%array.length];
        }
    }

    public static void swapArrays(int[] first, int[] second) {
        int[] temp = first.clone();
        System.arraycopy(second, 0, first, 0, first.length);
        System.arraycopy(temp, 0, second, 0, first.length);
    }

    public static boolean isEmpty(int[] array) {
        for (int value : array) {
            if (value != 0) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean contains(T value, T[] values) {
        return Arrays.asList(values).contains(value);
    }

    public static int numNonNull(Object... objects) {
        return (int)Arrays.stream(objects)
                          .filter(Objects::nonNull)
                          .count();
    }

    public static <T> T wrapIncrementValue(T[] values, int currentIndex, int incrementAmount) {
        return values[wrapIncrement(currentIndex, incrementAmount, values.length)];
    }

    public static int wrapIncrement(int previousAmount, int incrementAmount, int minValue, int maxValue) {
        int amount = previousAmount;

        amount -= minValue;             // Set to be zero indexed
        amount += incrementAmount;      // Increment by the specified amount
        amount += maxValue;             // Confirm positive (BECAUSE CS IS STUPID AND MOD DOESN'T WORK RIGHT)
        amount %= maxValue;             // Apply wrap around
        amount += minValue;             // Set back to original index

        return amount;
    }

    // Zero-indexed wrap increment
    public static int wrapIncrement(int previousAmount, int incrementAmount, int maxValue) {
        return wrapIncrement(previousAmount, incrementAmount, 0, maxValue);
    }

    public static int getIntegerValue(Integer integer) {
        return integer == null ? 0 : integer;
    }

    public static boolean getBooleanValue(Boolean value) {
        return value == null ? false : value;
    }

    public static boolean parseBoolean(String booleanString) {
        switch (booleanString.toLowerCase()) {
            case "true":
            case "yes":
                return true;
            case "false":
            case "no":
                return false;
            default:
                Global.error("Invalid boolean type: " + booleanString);
                return false;
        }
    }

    public static int max(double... values) {
        double max = values[0];
        for (double value : values) {
            if (value > max) {
                max = value;
            }
        }

        return (int)max;
    }

    public static int[] sixIntArray(Scanner in) {
        int[] arr = new int[6];
        for (int i = 0; i < 6; i++) {
            arr[i] = in.nextInt();
        }

        return arr;
    }

    public static <T extends Enum<T>> List<T> arrayValueOf(Class<T> enumType, String[] contents) {
        return Arrays.stream(contents)
                     .map(value -> Enum.valueOf(enumType, StringUtils.getNamesiesString(value)))
                     .collect(Collectors.toList());
    }

    public static <T> T getPageValue(Iterable<T> list, int pageNum, int buttonsPerPage, int index) {
        Iterator<T> iterator = pageIterator(list, pageNum, buttonsPerPage);
        for (int i = 0; i < index; i++) {
            iterator.next();
        }

        return iterator.next();
    }

    public static <T> Iterator<T> pageIterator(Iterable<T> list, int pageNum, int buttonsPerPage) {
        Iterator<T> iterator = list.iterator();
        for (int i = 0; i < pageNum*buttonsPerPage && iterator.hasNext(); i++) {
            iterator.next();
        }

        return iterator;
    }

    public static <T> List<T> combine(List<T> firstList, List<T> secondList) {
        List<T> list = new ArrayList<>();
        list.addAll(firstList);
        list.addAll(secondList);

        return list;
    }

    public static <T> List<T> inFirstNotSecond(List<T> first, List<T> second) {
        List<T> missing = new ArrayList<>();
        for (T value : first) {
            if (!second.contains(value)) {
                missing.add(value);
            }
        }
        return missing;
    }

    @SafeVarargs
    public static <T> T[] append(T[] base, T... extraArgs) {
        T[] array = Arrays.copyOf(base, base.length + extraArgs.length);
        System.arraycopy(extraArgs, 0, array, base.length, array.length - base.length);
        return array;
    }

    public static int getTotalPages(int totalItems, int itemsPerPage) {
        return Math.max(1, (int)Math.ceil((double)totalItems/itemsPerPage));
    }

    // Returns the estimated number of trials needed to get all n objects with probability 1/n
    // with confidence p (between 0 and 1)
    public static int numTrials(double p, int n) {
        return (int)Math.ceil(-n * Math.log((1 - p)/n));
    }

    public static boolean hasDeclaredMethod(Class<?> classy, String methodName, Class<?>... parameterTypes) {
        try {
            // This will throw a NoSuchMethodException if the classy does not have the specified method
            classy.getDeclaredMethod(methodName, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
