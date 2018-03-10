package pattern.action;

import item.ItemNamesies;
import map.triggers.ChoiceTrigger;
import map.triggers.GiveItemTrigger;
import map.triggers.GivePokemonTrigger;
import map.triggers.TradePokemonTrigger;
import map.triggers.Trigger;
import map.triggers.UseItemTrigger;
import map.triggers.map.MoveNPCTrigger;
import mapMaker.dialogs.action.ActionType;
import util.serialization.JsonMatcher;
import pattern.PokemonMatcher;
import pokemon.PokemonNamesies;

public interface ActionMatcher extends JsonMatcher {
    ActionType getActionType();
    Trigger createNewTrigger();

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
        public Trigger createNewTrigger() {
            return new GivePokemonTrigger(pokemonMatcher);
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
        public Trigger createNewTrigger() {
            return new GiveItemTrigger(this.giveItem, this.quantity);
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
            // Not available in map maker since it makes no sense there
            return null;
        }

        @Override
        public Trigger createNewTrigger() {
            return new UseItemTrigger(this.useItem);
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
        public Trigger createNewTrigger() {
            return new MoveNPCTrigger(this);
        }
    }

    class ChoiceActionMatcher implements ActionMatcher {
        private String question;
        private ChoiceMatcher[] choices;

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
        public Trigger createNewTrigger() {
            return new ChoiceTrigger(this);
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
        public Trigger createNewTrigger() {
            return new TradePokemonTrigger(tradePokemon, requested);
        }
    }
}
