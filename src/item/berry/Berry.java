package item.berry;

import item.hold.ConsumableItem;
import type.Type;

public interface Berry extends ConsumableItem {
	Type naturalGiftType();
	int naturalGiftPower();

	default double getHarvestHours() {
		return 1;
	}

	default int getHarvestAmount() {
		return 3;
	}

	@Override
	default int flingDamage() {
		return 10;
	}
}
