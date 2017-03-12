package util;

import java.util.Calendar;

public class TimeUtils {
    public static int getHourOfDay() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static boolean currentHourWithinInterval(int startHour, int endHour) {
        int hour = getHourOfDay();
        return hour >= startHour && hour < endHour;
    }
}
