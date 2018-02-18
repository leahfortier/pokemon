package pattern.action;

import map.triggers.TriggerType;
import pattern.PokemonMatcher;
import pattern.action.ChoiceActionMatcher.ChoiceMatcher;

public class ActionMatcher2 {

    public static class TriggerActionMatcher2 extends ActionMatcher2 {
        public TriggerType triggerType;
        public String triggerContents;
    }

    public static class BattleActionMatcher extends ActionMatcher2 {
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
    }

    public static class ChoiceActionMatcher2 extends ActionMatcher2 {
        public String question;
        public ChoiceMatcher[] choices;

        public ChoiceActionMatcher2(String question, ChoiceMatcher[] choices) {
            this.question = question;
            this.choices = choices;
        }
    }

    public static class UpdateActionMatcher extends ActionMatcher2 {
        public String update;
    }

    public static class GroupTriggerActionMatcher extends ActionMatcher2 {
        public String groupTrigger;
    }

    public static class GlobalActionMatcher extends ActionMatcher2 {
        public String global;
    }
}
