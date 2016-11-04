package map.triggers;

import main.Game;
import main.Global;
import trainer.CharacterData;

public class BadgeTrigger extends Trigger {
	private final int badgeIndex;

	BadgeTrigger(String badgeIndex) {
		this(badgeIndex, TriggerType.BADGE.getTriggerName(badgeIndex));
	}

	private BadgeTrigger(String badgeIndex, String triggerName) {
		super(TriggerType.BADGE, badgeIndex, "!" + triggerName, triggerName);

		this.badgeIndex = Integer.parseInt(badgeIndex);
		if (this.badgeIndex < 0 || this.badgeIndex >= CharacterData.NUM_BADGES) {
			Global.error("Invalid badge index " + this.badgeIndex);
		}
	}

	protected void executeTrigger() {
		Game.getPlayer().giveBadge(this.badgeIndex);
	}
}
