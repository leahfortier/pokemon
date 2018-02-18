package map.triggers;

import main.Game;
import map.condition.Condition;
import map.condition.Condition.BadgeCondition;
import map.condition.ConditionHolder.AndCondition;
import map.condition.ConditionHolder.NotCondition;
import trainer.player.Badge;

public class BadgeTrigger extends Trigger {
    private final Badge badge;

    BadgeTrigger(String badgeName, Condition condition) {
        // Can't get the same badge twice
        super(TriggerType.BADGE, badgeName, new AndCondition(condition, new NotCondition(new BadgeCondition(badgeName))));
        this.badge = Badge.valueOf(badgeName);
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().giveBadge(this.badge);
    }
}
