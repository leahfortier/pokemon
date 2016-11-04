package map.entity.npc;

import map.triggers.Trigger;
import map.triggers.TriggerType;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.BattleMatcher;
import pattern.AreaDataMatcher.GroupTriggerMatcher;
import pattern.AreaDataMatcher.UpdateMatcher;
import util.StringUtils;

public abstract class NPCAction {
    public Trigger getTrigger(String npcEntityName) {
        return this.getTriggerType()
                .createTrigger(this.getTriggerContents(npcEntityName));
    }

    protected abstract TriggerType getTriggerType();
    protected abstract String getTriggerContents(String npcEntityName);

    public static class TriggerAction extends NPCAction {
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
        protected String getTriggerContents(String npcEntityName) {
            return this.contents;
        }
    }

    public static class BattleAction extends NPCAction {
        public String name;
        public int cashMoney;
        public String[] pokemon;
        public String updateInteraction;
        public String npcEntityName;

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
        protected String getTriggerContents(String npcEntityName) {
            this.npcEntityName = npcEntityName;
            return AreaDataMatcher.getJson(this);
        }
    }

    public static class UpdateAction extends NPCAction {
        private final String interactionName;

        public UpdateAction(final String interactionName) {
            this.interactionName = interactionName;
        }

        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.UPDATE;
        }

        @Override
        protected String getTriggerContents(String npcEntityName) {
            UpdateMatcher matcher = new UpdateMatcher(npcEntityName, interactionName);
            return AreaDataMatcher.getJson(matcher);
        }
    }

    public static class GroupTriggerAction extends NPCAction {
        private final String triggerName;

        public GroupTriggerAction(final String triggerName) {
            this.triggerName = triggerName;
        }

        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.GROUP;
        }

        @Override
        protected String getTriggerContents(String npcEntityName) {
            GroupTriggerMatcher matcher = new GroupTriggerMatcher(triggerName);
            return AreaDataMatcher.getJson(matcher);
        }
    }

    // TODO: This
    public static class ChoiceAction extends NPCAction {
        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.DIALOGUE;
        }

        @Override
        protected String getTriggerContents(String npcEntityName) {
            return StringUtils.empty();
        }
    }
}
