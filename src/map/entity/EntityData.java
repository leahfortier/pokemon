package map.entity;

import map.Condition;
import pattern.EntityMatcher;

public abstract class EntityData {
	public String name;
	public Condition condition;
	
	public int x;
	public int y;

	protected EntityData(EntityMatcher matcher) {
		this(matcher, null);
	}

	protected EntityData(EntityMatcher matcher, String condition) {
		this.name = matcher.getTriggerName();
		this.condition = new Condition(Condition.and(matcher.getCondition(), condition));
	}

	public abstract Entity getEntity();

	public boolean isEntityPresent() {
		return condition.isTrue();
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
