package map.entity;

import pattern.AreaDataMatcher.ItemMatcher;

// TODO: This whole class can be deleted and make sure the getEntity method is just moved to the ItemMatcher class
public class ItemEntityData extends EntityData {
	private ItemEntity entity;
	private String item;

	public ItemEntityData(ItemMatcher matcher) {
		super(matcher.getName(), "!has" + matcher.getName());

		this.x = matcher.getX();
		this.y = matcher.getY();
		this.item = matcher.getItemName();
	}

	public Entity getEntity() {
		if (entity == null) {
			entity = new ItemEntity(name, x, y, item);
		}
		
		return entity;
	}
	
	public String getItem() {
		return item;
	}
}
