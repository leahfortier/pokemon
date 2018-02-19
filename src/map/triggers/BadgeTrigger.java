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
        this(Badge.valueOf(badgeName), condition);
    }

    BadgeTrigger(Badge badge, Condition condition) {
        // Can't get the same badge twice
        super(badge.name(), new AndCondition(condition, new NotCondition(new BadgeCondition(badge))));
        this.badge = badge;
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().giveBadge(this.badge);
    }
}
