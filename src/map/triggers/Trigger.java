package map.triggers;

import main.Game;
import map.Condition;

import java.util.ArrayList;
import java.util.List;

public abstract class Trigger {

	protected final String name;
	protected final Condition condition;
	private final List<String> globals;

	protected Trigger(TriggerType type, String contents, String condition) {
		this(type, contents, condition, null);
	}

	protected Trigger(TriggerType type, String contents, String condition, List<String> globals) {
		this.name = type.getTriggerName(contents);

		this.condition = new Condition(condition);

		this.globals = new ArrayList<>();
		if (globals != null) {
			this.globals.addAll(globals);
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
