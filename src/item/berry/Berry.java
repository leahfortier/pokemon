package item.berry;

import item.hold.ConsumableItem;
import item.hold.HoldItem;
import main.Type;

public interface Berry extends ConsumableItem, HoldItem
{
	public Type naturalGiftType();
	public int naturalGiftPower();
}
