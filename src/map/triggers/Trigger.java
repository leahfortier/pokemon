package map.triggers;

import main.Game;
import map.Condition;
import trainer.CharacterData;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Trigger {
	private static final Pattern globalPattern = Pattern.compile("global:\\s*([!]?\\w+)");
	protected static final Pattern variablePattern = Pattern.compile("(\\w+):\\s*([\\w -.']+)", Pattern.UNICODE_CHARACTER_CLASS);
	
	protected final String name;
	protected final Condition condition;

	protected final List<String> globals;

	public Trigger(String name, String str) {
		this.name = name;
		
		condition = new Condition(str);
		
		globals = new ArrayList<>();
		Matcher m = globalPattern.matcher(str);
	
		while (m.find()) {
			globals.add(m.group(1));
		}		
	}

	public static Trigger createTrigger(TriggerType type, String name, String contents) {
		return type.getTrigger(name, contents);
	}
	
	// Evaluate the function, Should only be triggered when a player moves
	// into a map square that is defined to trigger this event
	public boolean isTriggered(CharacterData data) {
		return condition.isTrue(data);
	}
	
	public void execute(Game game) {
		for (String s: globals) {
			if (s.charAt(0) == '!') {
				game.characterData.removeGlobal(s.substring(1));
			}
			else {
				game.characterData.addGlobal(s);
			}
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String triggerDataAsString() {
		StringBuilder ret = new StringBuilder();
		
		if (!condition.getOriginalConditionString().isEmpty()) {
			ret.append("\tcondition: ")
					.append(condition.getOriginalConditionString())
					.append("\n");
		}
		
		for (String global: globals) {
			ret.append("\tglobal: ")
					.append(global)
					.append("\n");
		}
		
		return ret.toString();
	}
	
	public Condition getCondition() {
		return condition;
	}
	
	public List<String> getGlobals() {
		return globals;
	}
}
