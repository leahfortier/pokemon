package map.entity;

import java.util.regex.Pattern;

import map.Condition;

public abstract class EntityData {
	public static final Pattern variablePattern = Pattern.compile("(\\w+):\\s*(-?[\\w'.-]+)", Pattern.UNICODE_CHARACTER_CLASS);
	
	public String name;
	public Condition condition;
	protected String trigger;
	
	public int x;
	public int y;

	// TODO: contents -> condition
	protected EntityData(String name, String contents) {
		this.name = name;
		condition = new Condition(contents);
	}

	public abstract Entity getEntity();
	public abstract String entityDataAsString();

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
