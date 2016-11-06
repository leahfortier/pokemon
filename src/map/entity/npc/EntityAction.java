package map.entity.npc;

import gui.GameData;
import main.Game;
import map.triggers.Trigger;
import map.triggers.TriggerData.Point;
import map.triggers.TriggerType;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.BattleMatcher;
import pattern.AreaDataMatcher.GroupTriggerMatcher;
import pattern.AreaDataMatcher.UpdateMatcher;
import util.StringUtils;

import java.util.List;

public abstract class EntityAction {

    public static Trigger addActionGroupTrigger(String entityName, String triggerSuffix, List<EntityAction> actions) {
        GameData data = Game.getData();

        final String[] actionTriggerNames = new String[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            Trigger actionTrigger = actions.get(i).getTrigger(entityName);
            data.addTrigger(actionTrigger);
            actionTriggerNames[i] = actionTrigger.getName();
        }

        GroupTriggerMatcher matcher = new GroupTriggerMatcher(actionTriggerNames);
        matcher.suffix = triggerSuffix;
        final String groupContents = AreaDataMatcher.getJson(matcher);

        // Condition is really in the interaction name and the npc condition, so
        // once these are both valid then the interaction can proceed as usual
        // TODO: Condition is inside group trigger, but now that this exists, should it be removed?
        Trigger groupTrigger = TriggerType.GROUP.createTrigger(groupContents, null);
        data.addTrigger(groupTrigger);
        return groupTrigger;
    }

    private Trigger getTrigger(String entityName) {
        return this.getTriggerType()
                .createTrigger(
                        this.getTriggerContents(entityName),
                        this.getCondition()
                );
    }

    protected abstract TriggerType getTriggerType();
    protected abstract String getTriggerContents(String entityName);
    protected String getCondition() {
        return null;
    }

    public static class TriggerAction extends EntityAction {
        private final TriggerType type;
        private final String contents;
        private final String condition;

        public TriggerAction(final TriggerType type, final String contents, final String condition) {
            this.type = type;
            this.contents = StringUtils.isNullOrEmpty(contents) ? StringUtils.empty() : contents;
            this.condition = condition;
        }

        @Override
        protected TriggerType getTriggerType() {
            return this.type;
        }

        @Override
        protected String getTriggerContents(String entityName) {
            return this.contents;
        }

        @Override
        protected String getCondition() {
            return this.condition;
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
