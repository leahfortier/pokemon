package map.entity;

import pattern.AreaDataMatcher.ItemMatcher;

public class ItemEntityData extends EntityData {
	private ItemEntity entity;
	private String item;

	public ItemEntityData(ItemMatcher matcher) {
		super(matcher.name, "!has" + matcher.name);

		x = matcher.x;
		y = matcher.y;
		item = matcher.item;
	}
	
	public ItemEntityData(String name, String conditionString, String item, int x, int y) {
		super (name, conditionString);
		
		entity = null;
		condition.add("!has" + name, '&');
		
		this.x = x;
		this.y = y;
		this.item = item;
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
