package map.entity;

import java.util.regex.Pattern;

import map.Condition;

public abstract class EntityData {
	public static final Pattern variablePattern = Pattern.compile("(\\w+):\\s*(-?[\\w'.-]+)", Pattern.UNICODE_CHARACTER_CLASS);
	
	public String name;
	public Condition condition;
	
	public int x;
	public int y;

		this.name = name;
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
