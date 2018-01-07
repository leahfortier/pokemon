package item.berry;

import item.hold.ConsumableItem;
import type.Type;

public interface Berry extends ConsumableItem {
    Type naturalGiftType();
    int naturalGiftPower();

    default int getHarvestHours() {
        return 24;
    }

    default int getHarvestAmount() {
        return 3;
    }

    @Override
    default int flingDamage() {
        return 10;
    }
}
