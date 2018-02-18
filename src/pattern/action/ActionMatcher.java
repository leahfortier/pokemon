package pattern.action;

import main.Global;
import map.condition.Condition;
import map.entity.EntityAction;
import map.entity.EntityAction.BattleAction;
import map.entity.EntityAction.ChoiceAction;
import map.entity.EntityAction.GlobalAction;
import map.entity.EntityAction.GroupTriggerAction;
import map.entity.EntityAction.TriggerAction;
import map.entity.EntityAction.UpdateAction;
import map.triggers.TriggerType;
import mapMaker.dialogs.action.ActionType;
import mapMaker.dialogs.action.trigger.TriggerActionType;
import pattern.PokemonMatcher;
import trainer.Trainer;

public abstract class ActionMatcher {
    public abstract ActionType getActionType();
    public abstract EntityAction getAction(final Condition condition);

    public String getActionString() {
        Global.error("Invalid action if this hasn't been overridden.");
        return null;
    }

    public static class TriggerActionMatcher extends ActionMatcher {
        public TriggerType triggerType;
        public String triggerContents;

        public TriggerActionMatcher(TriggerType triggerType, String triggerContents) {
            this.triggerType = triggerType;
            this.triggerContents = triggerContents;
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
        public EntityAction getAction(Condition condition) {
            return new TriggerAction(triggerType, triggerContents, condition);
        }
    }

    public static class BattleActionMatcher extends ActionMatcher {
        public String name;
        public int cashMoney;
        public boolean maxPokemonLimit;
        public PokemonMatcher[] pokemon;
        public String update;

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

        @Override
        public ActionType getActionType() {
            return ActionType.BATTLE;
        }

        @Override
        public EntityAction getAction(Condition condition) {
            return new BattleAction(this);
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
        public EntityAction getAction(Condition condition) {
            return new ChoiceAction(this);
        }
    }

    public static class UpdateActionMatcher extends ActionMatcher {
        public String update;

        public UpdateActionMatcher(String update) {
            this.update = update;
        }

        @Override
        public String getActionString() {
            return this.update;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.UPDATE;
        }

        @Override
        public EntityAction getAction(Condition condition) {
            return new UpdateAction(update);
        }
    }

    public static class GroupTriggerActionMatcher extends ActionMatcher {
        public String groupTrigger;

        public GroupTriggerActionMatcher(String groupTrigger) {
            this.groupTrigger = groupTrigger;
        }

        @Override
        public String getActionString() {
            return this.groupTrigger;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.GROUP_TRIGGER;
        }

        @Override
        public EntityAction getAction(Condition condition) {
            return new GroupTriggerAction(groupTrigger);
        }
    }

    public static class GlobalActionMatcher extends ActionMatcher {
        public String global;

        public GlobalActionMatcher(String global) {
            this.global = global;
        }

        @Override
        public String getActionString() {
            return this.global;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.GLOBAL;
        }

        @Override
        public EntityAction getAction(Condition condition) {
            return new GlobalAction(global);
        }
    }
}
