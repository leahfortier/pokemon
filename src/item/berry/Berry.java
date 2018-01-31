package item.berry;

import item.hold.HoldItem;
import type.Type;

public interface Berry extends HoldItem {
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
