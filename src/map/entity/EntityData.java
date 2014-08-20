package map.entity;

import java.util.regex.Pattern;

import map.Condition;
import trainer.CharacterData;

public abstract class EntityData {
	public static final Pattern variablePattern = Pattern.compile("(\\w+):\\s*(-?\\w+)", Pattern.UNICODE_CHARACTER_CLASS);
	
	public String name;
	public Condition condition;
	protected String trigger;
	
	public int x;
	public int y;
	
	public EntityData(String name, String contents){
		this.name = name;
		condition = new Condition(contents);
	}
	public boolean isEntityPresent(CharacterData data){
		return condition.isTrue(data);
	}
	public abstract Entity getEntity();
	
//	public String getName() {
//		return name;
//	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public abstract String entityDataAsString(); 
}
