package item.berry.farm;

import item.ItemNamesies;
import item.berry.Berry;
import util.TimeUtils;

public class BerryStats {
    private final Berry berry;
    private long timestamp;

    BerryStats(final ItemNamesies berry) {
        this.berry = (Berry)berry.getItem();
        this.timestamp = TimeUtils.getCurrentTimestamp();
    }

    ItemNamesies getBerryKind() {
        return berry.namesies();
    }

    boolean isFinished() {
        // TODO: Make this dependent on the berry :D
        return TimeUtils.numDaysPassed(this.timestamp) > 1;
    }

    int getHarvestAmount() {
        // TODO: Make this dependent on the berry :D
        return 3;
    }
}
