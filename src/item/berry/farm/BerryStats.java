package item.berry.farm;

import item.ItemNamesies;
import item.berry.Berry;
import util.TimeUtils;

import java.io.Serializable;

public class BerryStats implements Serializable {
    private final Berry berry;
    private long timestamp;

    BerryStats(final ItemNamesies berry) {
        this.berry = (Berry)berry.getItem();
        this.timestamp = TimeUtils.getCurrentTimestamp();
    }

    public Berry getBerry() {
        return berry;
    }

    boolean isFinished() {
        return TimeUtils.numDaysPassed(this.timestamp) > berry.getHarvestHours();
    }

    int getHarvestAmount() {
        return berry.getHarvestAmount();
    }

    public String getTimeLeftString() {
        int totalMinutes = (int)(berry.getHarvestHours()*TimeUtils.MINUTES_IN_DAY);
        int minutesPassed = TimeUtils.numMinutesPassed(this.timestamp);
        int minutesLeft = totalMinutes - minutesPassed;
        return String.format("%02d:%02d", minutesLeft/60, minutesLeft%60);
    }
}
