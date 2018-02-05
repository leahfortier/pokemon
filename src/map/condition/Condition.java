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

        public BadgeCondition(String badgeName) {
            this.badge = Badge.valueOf(badgeName);
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

    class NpcInteractionCondition implements Condition {
        private final String npcEntityName;
        private final String interactionName;

        public NpcInteractionCondition(String npcEntityName, String interactionName) {
            this.npcEntityName = npcEntityName;
            this.interactionName = interactionName;
        }

        public String getNpcEntityName() {
            return this.npcEntityName;
        }

        public String getInteractionName() {
            return this.interactionName;
        }

        @Override
        public boolean evaluate() {
            return Game.getPlayer().isNpcInteraction(npcEntityName, interactionName);
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
