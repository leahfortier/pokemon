package map.triggers;

import main.Game;
import map.condition.Condition.BadgeCondition;
import map.condition.ConditionHolder.NotCondition;
import trainer.player.Badge;

public class BadgeTrigger extends Trigger {
    private final Badge badge;

    public BadgeTrigger(Badge badge) {
        // Can't get the same badge twice
        super(badge.name(), new NotCondition(new BadgeCondition(badge)));
        this.badge = badge;
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().giveBadge(this.badge);
    }
}
