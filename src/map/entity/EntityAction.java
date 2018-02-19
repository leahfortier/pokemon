package map.entity;

import map.condition.Condition;
import map.condition.ConditionSet;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import pattern.GroupTriggerMatcher;
import pattern.JsonMatcher;
import pattern.action.ActionMatcher.BattleActionMatcher;
import pattern.action.ActionMatcher.ChoiceActionMatcher;
import pattern.action.UpdateMatcher;
import util.StringUtils;

public abstract class EntityAction {
    protected abstract TriggerType getTriggerType();
    protected abstract String getTriggerContents(String entityName);
    protected Condition getCondition() { return null; }

    private Trigger getTrigger(String entityName) {
        return this.getTriggerType()
                   .createTrigger2(
                           this.getTriggerContents(entityName),
                           this.getCondition()
                   );
    }

    public static class TriggerAction extends EntityAction {
        private final TriggerType type;
        private final String contents;
        private final ConditionSet condition;

        public TriggerAction(final TriggerType type, final String contents, final Condition condition) {
            this.type = type;
            this.contents = StringUtils.isNullOrEmpty(contents) ? StringUtils.empty() : contents;
            this.condition = new ConditionSet(condition);
        }

        @Override
        public TriggerType getTriggerType() {
            return this.type;
        }

        @Override
        public String getTriggerContents(String entityName) {
            return this.contents;
        }

        @Override
        protected Condition getCondition() {
            return this.condition.getCondition();
        }
    }

    public static class BattleAction extends EntityAction implements JsonMatcher {
        private final BattleActionMatcher battleMatcher;
        private String entityName;

        public BattleAction(BattleActionMatcher matcher) {
            this.battleMatcher = matcher;
        }

        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.TRAINER_BATTLE;
        }

        @Override
        protected String getTriggerContents(String entityName) {
            this.entityName = entityName;
            return this.getJson();
        }

        public BattleActionMatcher getBattleMatcher() {
            return this.battleMatcher;
        }

        public String getEntityName() {
            return this.entityName;
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
            return matcher.getJson();
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
            return matcher.getJson();
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

    public static class ChoiceAction extends EntityAction {
        private final ChoiceActionMatcher matcher;

        public ChoiceAction(final ChoiceActionMatcher matcher) {
            this.matcher = matcher;
        }

        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.CHOICE;
        }

        @Override
        protected String getTriggerContents(String entityName) {
            return matcher.getJson();
        }
    }
}
