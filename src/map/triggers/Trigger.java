package map.triggers;

import main.Game;
import main.Global;
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

	public static Trigger createTrigger(String type, String name, String contents) {
		return TriggerType.getTrigger(type, name, contents);
	}

	private enum TriggerType {
		BADGE("Badge", BadgeTrigger::new),
		CHANGE_VIEW("ChangeView", ChangeViewTrigger::new),
		EVENT("Event", EventTrigger::new),
		GIVE("Give", GiveTrigger::new),
		GROUP("Group", GroupTrigger::new),
		HEAL_PARTY("HealParty", HealPartyTrigger::new),
		LAST_POKE_CENTER("LastPokeCenter", LastPokeCenterTrigger::new),
		MAP_TRANSITION("MapTransition", MapTransitionTrigger::new),
		SOUND("Sound", SoundTrigger::new),
		TRAINER_BATTLE("TrainerBattle", TrainerBattleTrigger::new),
		WILD_BATTLE("WildBattle", WildBattleTrigger::new);

		final String typeName;
		final GetTrigger getTrigger;

		TriggerType(final String typeName, final GetTrigger getTrigger) {
			this.typeName = typeName;
			this.getTrigger = getTrigger;
		}

		private interface GetTrigger {
			Trigger getTrigger(final String name, final String contents);
		}

		public static Trigger getTrigger(final String type, final String name, final String contents) {
			for (final TriggerType triggerType : TriggerType.values()) {
				if (triggerType.typeName.equals(type)) {
					return triggerType.getTrigger.getTrigger(name, contents);
				}
			}

			Global.error("Could not find a trigger with type " + type + ". Name: " + name + ", Contents: " + contents);
			return null;
		}
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
