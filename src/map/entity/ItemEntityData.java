package map.entity;

import namesies.ItemNamesies;
import pattern.map.ItemMatcher;
import util.Point;

// TODO: This whole class can be deleted and make sure the getEntity method is just moved to the ItemMatcher class
public class ItemEntityData extends EntityData {
	private ItemEntity entity;
	private ItemNamesies item;

	public ItemEntityData(ItemMatcher matcher) {
		super(matcher, "!has" + matcher.getTriggerName());

		Point point = matcher.getLocation();
		x = point.x;
		y = point.y;
		this.item = matcher.getItem();
	}

	public Entity getEntity() {
		if (entity == null) {
			entity = new ItemEntity(name, x, y, item);
		}
		
		return entity;
	}
	
	public ItemNamesies getItem() {
		return item;
	}
}
