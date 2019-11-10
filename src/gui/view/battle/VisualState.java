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
    INVALID_BAG(BAG.visualStateHandler),
    FIGHT(new FightState()),
    INVALID_FIGHT(FIGHT.visualStateHandler),
    POKEMON(new PokemonState()),
    INVALID_POKEMON(POKEMON.visualStateHandler),
    MENU(new MenuState()),
    LEARN_MOVE(new LearnMoveState()),
    USE_ITEM(POKEMON.visualStateHandler),
    STAT_GAIN(MESSAGE.visualStateHandler),
    LOG_VIEW(new LogState());

    private final VisualStateHandler visualStateHandler;

    VisualState(VisualStateHandler visualStateHandler) {
        this.visualStateHandler = visualStateHandler;
    }

    public VisualStateHandler handler() {
        return this.visualStateHandler;
    }

    public static ItemNamesies getSelectedItem() {
        return ((BagState)BAG.handler()).getSelectedItem();
    }

    public static void setSwitchForced() {
        ((PokemonState)POKEMON.handler()).setSwitchForced();
    }
}
