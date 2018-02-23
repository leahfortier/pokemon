package pattern.action;

import map.triggers.Trigger;
import map.triggers.UpdateTrigger;
import map.triggers.battle.TrainerBattleTrigger;
import mapMaker.dialogs.action.ActionType;
import pattern.PokemonMatcher;
import trainer.Trainer;

public abstract class EntityActionMatcher implements ActionMatcher {
    private transient String entityName;

    public final void setEntity(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public static class BattleActionMatcher extends EntityActionMatcher {
        private String name;
        private int cashMoney;
        private boolean maxPokemonLimit;
        private PokemonMatcher[] pokemon;
        private String update;

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
        public Trigger createNewTrigger() {
            return new TrainerBattleTrigger(this);
        }
    }

    public static class UpdateActionMatcher extends EntityActionMatcher implements StringActionMatcher {
        private String interactionName;

        public UpdateActionMatcher(String interactionName) {
            this.interactionName = interactionName;
        }

        @Override
        public String getStringValue() {
            return this.interactionName;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.UPDATE;
        }

        @Override
        public Trigger createNewTrigger() {
            return new UpdateTrigger(new UpdateMatcher(this.getEntityName(), interactionName));
        }
    }
}
