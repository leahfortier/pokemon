package map.triggers;

import main.Game;
import main.Global;
import map.Condition;
import trainer.CharacterData;

import java.util.Collections;

public class BadgeTrigger extends Trigger {
	private final int badgeIndex;

	BadgeTrigger(String badgeIndex, String condition) {
		this(badgeIndex, condition, TriggerType.BADGE.getTriggerName(badgeIndex));
	}

	private BadgeTrigger(String badgeIndex, String condition, String triggerName) {
		super(TriggerType.BADGE, badgeIndex, Condition.and(condition, "!" + triggerName), Collections.singletonList(triggerName));

		this.badgeIndex = Integer.parseInt(badgeIndex);
		if (this.badgeIndex < 0 || this.badgeIndex >= CharacterData.NUM_BADGES) {
			Global.error("Invalid badge index " + this.badgeIndex);
		}
	}

	protected void executeTrigger() {
		Game.getPlayer().giveBadge(this.badgeIndex);
	}
}
