package util;

import java.util.Calendar;

public class TimeUtils {
    public static final int MILLSECONDS_IN_DAY = 1000*60*60*24;

    public static int getHourOfDay() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static boolean currentHourWithinInterval(int startHour, int endHour) {
        int hour = getHourOfDay();
        return hour >= startHour && hour < endHour;
    }

    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    public static double numDaysPassed(long timestamp) {
        long currentTimestamp = getCurrentTimestamp();
        return (currentTimestamp - timestamp)/(double)MILLSECONDS_IN_DAY;
    }
}
