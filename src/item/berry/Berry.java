package item.berry;

import item.hold.ConsumableItem;
import item.hold.HoldItem;
import type.Type;

public interface Berry extends ConsumableItem, HoldItem {
	Type naturalGiftType();
	int naturalGiftPower();

	@Override
	default int flingDamage() {
		return 10;
	}
}
