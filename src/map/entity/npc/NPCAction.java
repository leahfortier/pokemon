package map.entity.npc;

import map.triggers.Trigger;
import map.triggers.TriggerType;
import namesies.ItemNamesies;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.BattleMatcher;

public abstract class NPCAction {
    public Trigger getTrigger(String baseTriggerName, String triggerNameSuffix) {
        return Trigger.createTrigger(
                this.getTriggerType(),
                baseTriggerName + triggerNameSuffix,
                this.getTriggerContents(baseTriggerName)
        );
    }

    protected abstract TriggerType getTriggerType();
    protected abstract String getTriggerContents(String baseTriggerName);

    public static class DialogueAction extends NPCAction {
        private final String text;

        public DialogueAction(final String dialogueText) {
            this.text = dialogueText;
        }


        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.EVENT;
        }

        @Override
        protected String getTriggerContents(String baseTriggerName) {
            return this.text;
        }
    }

    public static class GiveItemAction extends NPCAction {
        private final ItemNamesies item;

        public GiveItemAction(final String itemName) {
            this.item = ItemNamesies.getValueOf(itemName);
        }

        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.GIVE;
        }

        @Override
        protected String getTriggerContents(String baseTriggerName) {
            return this.item.getName();
        }
    }

    public static class TriggerAction extends NPCAction {
        private final TriggerType type;
        private final String name;
        private final String contents;

        public TriggerAction(final TriggerType type, final String name, final String contents) {
            this.type = type;
            this.name = name;
            this.contents = contents;
        }

        @Override
        public Trigger getTrigger(String baseTriggerName, String triggerNameSuffix) {
            return Trigger.createTrigger(type, name, contents);
        }

        @Override
        protected TriggerType getTriggerType() {
            return this.type;
        }

        @Override
        protected String getTriggerContents(String baseTriggerName) {
            return this.contents;
        }
    }

    public static class GivePokemonAction extends NPCAction {
        private final String pokemonDescription;

        public GivePokemonAction(final String pokemonDescription) {
            this.pokemonDescription = pokemonDescription;
        }

        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.GIVE;
        }

        @Override
        protected String getTriggerContents(String baseTriggerName) {
            return this.pokemonDescription;
        }
    }

    public static class BattleAction extends NPCAction {
        public String name;
        public int cashMoney;
        public String[] pokemon;
        public String update;

        public String winGlobal;

        public BattleAction(BattleMatcher matcher) {
            this.name = matcher.name;
            this.cashMoney = matcher.cashMoney;
            this.pokemon = matcher.pokemon;
            this.update = matcher.update;
        }

        @Override
        protected TriggerType getTriggerType() {
            return TriggerType.TRAINER_BATTLE;
        }

        @Override
        protected String getTriggerContents(String baseTriggerName) {
            this.winGlobal = "triggered_" + baseTriggerName;

            return AreaDataMatcher.getJson(this);
        }
    }
}
