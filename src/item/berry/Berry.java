package item.berry;

import item.hold.ConsumedItem;
import item.hold.HoldItem;
import main.Type;

public interface Berry extends ConsumedItem, HoldItem
{
	public Type naturalGiftType();
	public int naturalGiftPower();
}
