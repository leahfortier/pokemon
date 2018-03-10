package item.berry.farm;

import item.ItemNamesies;
import item.berry.Berry;
import util.TimeUtils;
import util.serialization.Serializable;

import java.util.concurrent.TimeUnit;

public class PlantedBerry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Berry berry;
    private final long timestamp;

    public PlantedBerry(final ItemNamesies berry) {
        this.berry = (Berry)berry.getItem();
        this.timestamp = TimeUtils.getCurrentTimestamp();
    }

    public Berry getBerry() {
        return berry;
    }

    public String getTimeLeftString() {
        long totalMinutes = TimeUnit.HOURS.toMinutes(berry.getHarvestHours());
        long minutesPassed = TimeUtils.minutesSince(this.timestamp);
        long minutesLeft = Math.max(0, totalMinutes - minutesPassed);
        return TimeUtils.formatMinutes(minutesLeft);
    }

    boolean isFinished() {
        return TimeUtils.hoursSince(this.timestamp) > berry.getHarvestHours();
    }

    int getHarvestAmount() {
        return berry.getHarvestAmount();
    }
}
