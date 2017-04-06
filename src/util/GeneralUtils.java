package util;

import main.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public final class GeneralUtils {

    // Util class -- cannot be instantiated
    private GeneralUtils() {}

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

    public static boolean hasOnlyOneNonEmpty(Object... objects) {
        return Arrays.stream(objects)
                .filter(object -> object != null)
                .count() == 1;
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

    public static <T> Iterator<T> pageIterator(Iterable<T> list, int pageNum, int buttonsPerPage) {
        Iterator<T> iterator = list.iterator();
        for (int i = 0; i < pageNum*buttonsPerPage; i++) {
            iterator.next();
        }

        return iterator;
    }

    public static <T> List<T> combine(List<T> firstList, List<T> secondList) {
        List<T> list = new ArrayList<T>();
        list.addAll(firstList);
        list.addAll(secondList);

        return list;
    }
}
