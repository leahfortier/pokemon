package map.entity;

import pattern.AreaDataMatcher.ItemMatcher;
import util.StringUtils;

import java.util.regex.Matcher;

public class ItemEntityData extends EntityData {
	private ItemEntity entity;
	private String item;
	
	public String placedCondition;

	public ItemEntityData(ItemMatcher matcher) {
		super(matcher.name, ""); // TODO: Condition? maybe? do these have conditions?

		x = matcher.x;
		y = matcher.y;
		item = matcher.item;
	}

	public ItemEntityData(String name, String contents) {
		super (name, contents);
		
		entity = null;
		placedCondition = condition.getOriginalConditionString();
		condition.add("!has" + name, '&');
		
		Matcher m = variablePattern.matcher(contents);
		while (m.find()) {
			switch (m.group(1)) {
				case "x":
					x = Integer.parseInt(m.group(2));
					break;
				case "y":
					y = Integer.parseInt(m.group(2));
					break;
				case "trigger":
					// TODO: Trigger is currently ignored
					trigger = m.group(2);
					break;
				case "item":
					item = m.group(2);
					break;
			}
		}
	}
	
	public ItemEntityData(String name, String conditionString, String item, int x, int y) {
		super (name, conditionString);
		
		entity = null;
		placedCondition = condition.getOriginalConditionString();
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

	public String entityDataAsString() {
		StringBuilder ret = new StringBuilder();
		StringUtils.appendLine(ret, "Item " +name +"{");
		
		if (!placedCondition.isEmpty()) {
			StringUtils.appendLine(ret, "\tcondition: " + placedCondition);
		}

		StringUtils.appendLine(ret, "\tx: " + x);
		StringUtils.appendLine(ret, "\ty: " + y);
		StringUtils.appendLine(ret, "\titem: "+ item);
		StringUtils.appendLine(ret, "}");
		
		return ret.toString();
	}
}
