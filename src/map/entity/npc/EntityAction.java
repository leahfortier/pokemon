package map.entity.npc;

import map.triggers.Trigger;
import map.triggers.TriggerType;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.BattleMatcher;
import pattern.AreaDataMatcher.GroupTriggerMatcher;
import pattern.AreaDataMatcher.UpdateMatcher;
import util.StringUtils;

public abstract class EntityAction {
    public Trigger getTrigger(String entityName) {
        return this.getTriggerType()
                .createTrigger(this.getTriggerContents(entityName));
    }

    protected abstract TriggerType getTriggerType();
    protected abstract String getTriggerContents(String entityName);

    public static class TriggerAction extends EntityAction {
        private final TriggerType type;
        private final String contents;

        public TriggerAction(final TriggerType type, final String contents) {
            this.type = type;
            this.contents = StringUtils.isNullOrEmpty(contents) ? StringUtils.empty() : contents;
        }

        @Override
        protected TriggerType getTriggerType() {
            return this.type;
        }

        @Override
        protected String getTriggerContents(String entityName) {
            return this.contents;
        }
    }

    public static class BattleAction extends EntityAction {
        public String name;
        public int cashMoney;
        public String[] pokemon;
        public String updateInteraction;
        public String entityName;

        public BattleAction(BattleMatcher matcher) {
            this.name = matcher.name;
            this.cashMoney = matcher.cashMoney;
            this.pokemon = matcher.pokemon;
            this.updateInteraction = matcher.update;
        }

        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.TRAINER_BATTLE;
        }

        @Override
        protected String getTriggerContents(String entityName) {
            this.entityName = entityName;
            return AreaDataMatcher.getJson(this);
        }
    }

    public static class UpdateAction extends EntityAction {
        private final String interactionName;

        public UpdateAction(final String interactionName) {
            this.interactionName = interactionName;
        }

        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.UPDATE;
        }

        @Override
        protected String getTriggerContents(String entityName) {
            UpdateMatcher matcher = new UpdateMatcher(entityName, interactionName);
            return AreaDataMatcher.getJson(matcher);
        }
    }

    public static class GroupTriggerAction extends EntityAction {
        private final String triggerName;

        public GroupTriggerAction(final String triggerName) {
            this.triggerName = triggerName;
        }

        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.GROUP;
        }

        @Override
        protected String getTriggerContents(String entityName) {
            GroupTriggerMatcher matcher = new GroupTriggerMatcher(triggerName);
            return AreaDataMatcher.getJson(matcher);
        }
    }

    public static class GlobalAction extends EntityAction {
        private final String globalName;

        public GlobalAction(final String globalName) {
            this.globalName = globalName;
        }

        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.GLOBAL;
        }

        @Override
        protected String getTriggerContents(String entityName) {
            return globalName;
        }
    }

    // TODO: This
    public static class ChoiceAction extends EntityAction {
        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.DIALOGUE;
        }

        @Override
        protected String getTriggerContents(String entityName) {
            return StringUtils.empty();
        }
    }
}
