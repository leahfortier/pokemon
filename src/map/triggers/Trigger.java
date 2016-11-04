package map.triggers;

import gui.GameData;
import main.Game;
import map.Condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Trigger {

	protected final String name;
	protected final Condition condition;
	protected final List<String> globals;

	protected Trigger(TriggerType type, String contents) {
		this(type, contents, null);
	}

	protected Trigger(TriggerType type, String contents, String condition, String... globals) {
		this.name = type.getTriggerName(contents);

		this.condition = new Condition(condition);
		this.globals = new ArrayList<>();

		if (globals != null) {
			this.globals.addAll(Arrays.asList(globals));
		}
	}
	
	// Evaluate the function, Should only be triggered when a player moves
	// into a map square that is defined to trigger this event
	public boolean isTriggered() {
		return condition.isTrue();
	}

	public String getName() {
		return this.name;
	}

	public Condition getCondition() {
		return condition;
	}
	
	public List<String> getGlobals() {
		return globals;
	}

	public final void execute() {
		for (String global: globals) {
			if (global.startsWith("!")) {
				Game.getPlayer().removeGlobal(global.substring(1));
			}
			else {
				Game.getPlayer().addGlobal(global);
			}
		}

		this.executeTrigger();
	}

	protected abstract void executeTrigger();
}
