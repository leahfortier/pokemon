package item.berry;

import item.hold.ConsumableItem;
import type.Type;

public interface Berry extends ConsumableItem {
	Type naturalGiftType();
	int naturalGiftPower();

	@Override
	default int flingDamage() {
		return 10;
	}
}
