package util;

import java.util.Calendar;

public class TimeUtils {
    private static final Calendar calendar = Calendar.getInstance();

    public static int getHourOfDay() {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static boolean currentHourWithinInterval(int startHour, int endHour) {
        int hour = getHourOfDay();
        return hour >= startHour && hour < endHour;
    }
}
