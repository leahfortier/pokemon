package map.triggers;

import main.Game;
import map.condition.Condition;
import map.condition.ConditionKey;
import trainer.player.Badge;

class BadgeTrigger extends Trigger {
	private final Badge badge;

	BadgeTrigger(String badgeName, String condition) {
		// Can't get the same badge twice
		super(TriggerType.BADGE, badgeName, Condition.and(condition, "!" + ConditionKey.BADGE.getConditionString(badgeName)));
		this.badge = Badge.valueOf(badgeName);
	}

	protected void executeTrigger() {
		Game.getPlayer().giveBadge(this.badge);
	}
}
