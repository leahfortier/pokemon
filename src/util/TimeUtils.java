package util;

import main.Global;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public final class TimeUtils {
    // Utility class -- should not be instantiated
    private TimeUtils() {
        Global.error(this.getClass().getSimpleName() + " class cannot be instantiated.");
    }

    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    public static int getHourOfDay() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static boolean currentHourWithinInterval(int startHour, int endHour) {
        int hour = getHourOfDay();
        return hour >= startHour && hour < endHour;
    }

    public static long hoursSince(long timestamp) {
        return TimeUnit.MILLISECONDS.toHours(getCurrentTimestamp() - timestamp);
    }

    public static long minutesSince(long timestamp) {
        return TimeUnit.MILLISECONDS.toMinutes(getCurrentTimestamp() - timestamp);
    }

    public static long secondsSince(long timestamp) {
        return TimeUnit.MILLISECONDS.toSeconds(getCurrentTimestamp() - timestamp);
    }

    public static String formatSeconds(long numSeconds) {
        return formatMinutes(TimeUnit.SECONDS.toMinutes(numSeconds));
    }

    public static String formatMinutes(long numMinutes) {
        return String.format("%d:%02d", numMinutes/60, numMinutes%60);
    }
}
