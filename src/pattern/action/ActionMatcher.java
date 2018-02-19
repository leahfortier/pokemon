package pattern.action;

import item.ItemNamesies;
import map.condition.Condition;
import map.condition.ConditionSet;
import map.triggers.ChoiceTrigger;
import map.triggers.GroupTrigger;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import map.triggers.UseItemTrigger;
import map.triggers.battle.TrainerBattleTrigger;
import mapMaker.dialogs.action.ActionType;
import mapMaker.dialogs.action.trigger.TriggerActionType;
import pattern.GroupTriggerMatcher;
import pattern.JsonMatcher;
import pattern.PokemonMatcher;
import trainer.Trainer;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ActionMatcher implements JsonMatcher {
    public abstract ActionType getActionType();

    protected abstract Trigger getTrigger(String entityName, Condition condition);

    public static Trigger addActionGroupTrigger(String entityName, String triggerSuffix, Condition condition, List<ActionMatcher> actions) {
        final Trigger[] actionTriggers = actions.stream()
                                                .map(action -> action.getTrigger(entityName, condition))
                                                .collect(Collectors.toList())
                                                .toArray(new Trigger[0]);

        GroupTriggerMatcher matcher = new GroupTriggerMatcher(triggerSuffix, actionTriggers);
        return new GroupTrigger(matcher, condition);
    }

    public static class UseItemActionMatcher extends ActionMatcher {
        private ItemNamesies useItem;

        public UseItemActionMatcher(ItemNamesies useItem) {
            this.useItem = useItem;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.USE_ITEM;
        }

        @Override
        protected Trigger getTrigger(String entityName, Condition condition) {
            return new UseItemTrigger(this.useItem, condition);
        }
    }

    public static class TriggerActionMatcher extends ActionMatcher {
        public TriggerType triggerType;
        public String triggerContents;
        private ConditionSet condition;

        public TriggerActionMatcher(TriggerType triggerType, String triggerContents, Condition condition) {
            this.triggerType = triggerType;
            this.triggerContents = triggerContents;
            this.condition = new ConditionSet(condition);
        }

        public TriggerType getTriggerType() {
            return this.getTriggerActionType().getTriggerType();
        }

        public TriggerActionType getTriggerActionType() {
            return TriggerActionType.getTriggerActionType(triggerType);
        }

        public String getTriggerContents() {
            return this.triggerContents;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.TRIGGER;
        }

        @Override
        protected Trigger getTrigger(String entityName, Condition condition) {
            return this.getTriggerType()
                       .createTrigger2(
                               this.getTriggerContents(),
                               condition
                       );
        }
    }

    public static class BattleActionMatcher extends ActionMatcher {
        public String name;
        public int cashMoney;
        public boolean maxPokemonLimit;
        public PokemonMatcher[] pokemon;
        public String update;
        private String entityName;

        public BattleActionMatcher(String name, int cashMoney, boolean maxPokemonLimit, PokemonMatcher[] pokemon, String update) {
            this.name = name;
            this.cashMoney = cashMoney;
            this.maxPokemonLimit = maxPokemonLimit;
            this.pokemon = pokemon;
            this.update = update;
        }

        public String getName() {
            return this.name;
        }

        public int getDatCashMoney() {
            return this.cashMoney;
        }

        public PokemonMatcher[] getPokemon() {
            return this.pokemon;
        }

        public String getUpdateInteraction() {
            return this.update;
        }

        public boolean isMaxPokemonLimit() {
            return this.maxPokemonLimit;
        }

        public int getMaxPokemonAllowed() {
            return this.maxPokemonLimit ? this.pokemon.length : Trainer.MAX_POKEMON;
        }

        public String getEntityName() {
            return this.entityName;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.BATTLE;
        }

        @Override
        protected Trigger getTrigger(String entityName, Condition condition) {
            this.entityName = entityName;
            return new TrainerBattleTrigger(this, condition);
        }
    }

    public static class ChoiceActionMatcher extends ActionMatcher {
        public String question;
        public ChoiceMatcher[] choices;

        public ChoiceActionMatcher(String question, ChoiceMatcher[] choices) {
            this.question = question;
            this.choices = choices;
        }

        public String getQuestion() {
            return this.question;
        }

        public ChoiceMatcher[] getChoices() {
            return this.choices;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.CHOICE;
        }

        @Override
        protected Trigger getTrigger(String entityName, Condition condition) {
            return new ChoiceTrigger(this, condition);
        }
    }
}
