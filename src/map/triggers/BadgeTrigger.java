package map.triggers;

import main.Game;
import map.Condition;
import trainer.Badge;

import java.util.Collections;

class BadgeTrigger extends Trigger {
	private final Badge badge;

	BadgeTrigger(String badgeName, String condition) {
		this(badgeName, condition, TriggerType.BADGE.getTriggerName(badgeName));
	}

	private BadgeTrigger(String badgeName, String condition, String triggerName) {
		super(TriggerType.BADGE, badgeName, Condition.and(condition, "!" + triggerName), Collections.singletonList(triggerName));
		this.badge = Badge.valueOf(badgeName);
	}

	protected void executeTrigger() {
		Game.getPlayer().giveBadge(this.badge);
	}
}
