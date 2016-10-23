package map.entity;

import java.util.regex.Pattern;

import map.Condition;
import trainer.CharacterData;

public abstract class EntityData {
	public static final Pattern variablePattern = Pattern.compile("(\\w+):\\s*(-?[\\w'.-]+)", Pattern.UNICODE_CHARACTER_CLASS);
	
	public String name;
	public Condition condition;
	protected String trigger;
	
	public int x;
	public int y;
	
	EntityData(String name, String contents) {
		this.name = name;
		condition = new Condition(contents);
	}

	public abstract Entity getEntity();
	public abstract String entityDataAsString();

	public boolean isEntityPresent(CharacterData data) {
		return condition.isTrue(data);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
