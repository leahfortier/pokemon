package gui.view.battle;

import gui.view.battle.handler.BagState;
import gui.view.battle.handler.FightState;
import gui.view.battle.handler.LearnMoveState;
import gui.view.battle.handler.LogState;
import gui.view.battle.handler.MenuState;
import gui.view.battle.handler.MessageState;
import gui.view.battle.handler.PokemonState;
import gui.view.battle.handler.VisualStateHandler;
import item.ItemNamesies;

// Contains the different types of states a battle can be in
public enum VisualState {
    MESSAGE(new MessageState()),
    BAG(new BagState()),
    FIGHT(new FightState()),
    INVALID_FIGHT(FIGHT.handler),
    POKEMON(new PokemonState()),
    INVALID_POKEMON(POKEMON.handler),
    MENU(new MenuState()),
    LEARN_MOVE(new LearnMoveState()),
    USE_ITEM(POKEMON.handler),
    STAT_GAIN(MESSAGE.handler),
    LOG_VIEW(new LogState());

    private final VisualStateHandler handler;

    VisualState(VisualStateHandler handler) {
        this.handler = handler;
    }

    public VisualStateHandler handler() {
        return this.handler;
    }

    public static ItemNamesies getSelectedItem() {
        return ((BagState)BAG.handler()).getSelectedItem();
    }

    public static void setSwitchForced() {
        ((PokemonState)POKEMON.handler()).setSwitchForced();
    }
}
