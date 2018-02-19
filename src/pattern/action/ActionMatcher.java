package pattern.action;

import item.ItemNamesies;
import map.condition.Condition;
import map.triggers.ChoiceTrigger;
import map.triggers.DayCareTrigger;
import map.triggers.GiveItemTrigger;
import map.triggers.GivePokemonTrigger;
import map.triggers.GroupTrigger;
import map.triggers.HealPartyTrigger;
import map.triggers.TradePokemonTrigger;
import map.triggers.Trigger;
import map.triggers.UseItemTrigger;
import map.triggers.battle.FishingTrigger;
import map.triggers.battle.TrainerBattleTrigger;
import map.triggers.map.MoveNPCTrigger;
import map.triggers.map.ReloadMapTrigger;
import mapMaker.dialogs.action.ActionType;
import pattern.GroupTriggerMatcher;
import pattern.JsonMatcher;
import pattern.PokemonMatcher;
import pattern.map.FishingMatcher;
import pokemon.PokemonNamesies;
import trainer.Trainer;

import java.util.List;

public interface ActionMatcher extends JsonMatcher {
    ActionType getActionType();
    Trigger createNewTrigger(String entityName, Condition condition);

    static Trigger addActionGroupTrigger(String entityName, String triggerSuffix, Condition condition, List<ActionMatcher> actions) {
        final Trigger[] actionTriggers = new Trigger[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            actionTriggers[i] = actions.get(i).createNewTrigger(entityName, null).addData();
        }

        GroupTriggerMatcher matcher = new GroupTriggerMatcher(triggerSuffix, actionTriggers);
        return new GroupTrigger(matcher, condition).addData();
    }

    class HealPartyActionMatcher implements ActionMatcher {
        @Override
        public ActionType getActionType() {
            return ActionType.HEAL_PARTY;
        }

        @Override
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new HealPartyTrigger(condition);
        }
    }

    class DayCareActionMatcher implements ActionMatcher {
        @Override
        public ActionType getActionType() {
            return ActionType.DAY_CARE;
        }

        @Override
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new DayCareTrigger(condition);
        }
    }

    class ReloadMapActionMatcher implements ActionMatcher {
        @Override
        public ActionType getActionType() {
            return ActionType.RELOAD_MAP;
        }

        @Override
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new ReloadMapTrigger(condition);
        }
    }

    class GivePokemonActionMatcher implements ActionMatcher {
        private PokemonMatcher pokemonMatcher;

        public GivePokemonActionMatcher(PokemonMatcher pokemonMatcher) {
            this.pokemonMatcher = pokemonMatcher;
        }

        public PokemonMatcher getPokemonMatcher() {
            return this.pokemonMatcher;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.GIVE_POKEMON;
        }

        @Override
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new GivePokemonTrigger(pokemonMatcher, condition);
        }
    }

    class GiveItemActionMatcher implements ActionMatcher {
        private ItemNamesies giveItem;
        private int quantity;

        public GiveItemActionMatcher(ItemNamesies giveItem, int quantity) {
            this.giveItem = giveItem;
            this.quantity = quantity;
        }

        public ItemNamesies getItem() {
            return this.giveItem;
        }

        public int getQuantity() {
            return this.quantity;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.GIVE_ITEM;
        }

        @Override
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new GiveItemTrigger(this.giveItem, this.quantity, condition);
        }
    }

    class UseItemActionMatcher implements ActionMatcher {
        private ItemNamesies useItem;

        public UseItemActionMatcher(ItemNamesies useItem) {
            this.useItem = useItem;
        }

        public ItemNamesies getItem() {
            return this.useItem;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.USE_ITEM;
        }

        @Override
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new UseItemTrigger(this.useItem, condition);
        }
    }

    class MoveNpcActionMatcher implements ActionMatcher {
        private String npcEntityName;
        private String endEntranceName;
        private boolean endLocationIsPlayer;

        public MoveNpcActionMatcher(String npcEntityName, String endEntranceName, boolean endLocationIsPlayer) {
            this.npcEntityName = npcEntityName;
            this.endLocationIsPlayer = endLocationIsPlayer;

            // Ending at the player and another entrance are mutually exclusive
            if (!endLocationIsPlayer) {
                this.endEntranceName = endEntranceName;
            }
        }

        public String getNpcEntityName() {
            return this.npcEntityName;
        }

        public String getEndEntranceName() {
            return this.endEntranceName;
        }

        public boolean endLocationIsPlayer() {
            return this.endLocationIsPlayer;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.MOVE_NPC;
        }

        @Override
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new MoveNPCTrigger(this, condition);
        }
    }

    class BattleActionMatcher implements ActionMatcher {
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
        public Trigger createNewTrigger(String entityName, Condition condition) {
            this.entityName = entityName;
            return new TrainerBattleTrigger(this, condition);
        }
    }

    class ChoiceActionMatcher implements ActionMatcher {
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
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new ChoiceTrigger(this, condition);
        }
    }

    class TradePokemonActionMatcher implements ActionMatcher {
        private PokemonNamesies tradePokemon;
        private PokemonNamesies requested;

        public TradePokemonActionMatcher(PokemonNamesies tradePokemon, PokemonNamesies requested) {
            this.tradePokemon = tradePokemon;
            this.requested = requested;
        }

        public PokemonNamesies getRequested() {
            return this.requested;
        }

        public PokemonNamesies getTradePokemon() {
            return this.tradePokemon;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.TRADE_POKEMON;
        }

        @Override
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new TradePokemonTrigger(tradePokemon, requested, condition);
        }
    }

    class FishingActionMatcher implements ActionMatcher {
        private FishingMatcher matcher;

        public FishingActionMatcher(FishingMatcher matcher) {
            this.matcher = matcher;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.FISHING;
        }

        @Override
        public Trigger createNewTrigger(String entityName, Condition condition) {
            return new FishingTrigger(matcher, condition);
        }
    }
}
