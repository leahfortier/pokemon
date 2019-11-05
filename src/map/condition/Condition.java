package map.condition;

import item.ItemNamesies;
import main.Game;
import map.daynight.DayCycle;
import trainer.player.Badge;
import util.TimeUtils;

public interface Condition {
    boolean evaluate();

    class GlobalCondition implements Condition {
        private final String globalName;

        public GlobalCondition(String globalName) {
            this.globalName = globalName;
        }

        public String getGlobalName() {
            return this.globalName;
        }

        @Override
        public boolean evaluate() {
            return Game.getPlayer().hasGlobal(this.globalName);
        }
    }

    class BadgeCondition implements Condition {
        private final Badge badge;

        public BadgeCondition(Badge badge) {
            this.badge = badge;
        }

        @Override
        public boolean evaluate() {
            return Game.getPlayer().hasBadge(badge);
        }
    }

    class TimeOfDayCondition implements Condition {
        private final DayCycle dayCycle;

        public TimeOfDayCondition(DayCycle dayCycle) {
            this.dayCycle = dayCycle;
        }

        public DayCycle getTimeOfDay() {
            return this.dayCycle;
        }

        @Override
        public boolean evaluate() {
            return DayCycle.getTimeOfDay() == dayCycle;
        }
    }

    class HourOfDayCondition implements Condition {
        private final int startHour;
        private final int endHour;

        public HourOfDayCondition(int startHour, int endHour) {
            this.startHour = startHour;
            this.endHour = endHour;
        }

        @Override
        public boolean evaluate() {
            return TimeUtils.currentHourWithinInterval(startHour, endHour);
        }
    }

    class InteractionCondition implements Condition {
        private final String entityName;
        private final String interactionName;

        public InteractionCondition(String entityName, String interactionName) {
            this.entityName = entityName;
            this.interactionName = interactionName;
        }

        public String getEntityName() {
            return this.entityName;
        }

        public String getInteractionName() {
            return this.interactionName;
        }

        @Override
        public boolean evaluate() {
            return Game.getPlayer().isEntityInteraction(entityName, interactionName);
        }
    }

    class ItemCondition implements Condition {
        private final ItemNamesies item;

        public ItemCondition(ItemNamesies item) {
            this.item = item;
        }

        @Override
        public boolean evaluate() {
            return Game.getPlayer().getBag().hasItem(item);
        }
    }
}
