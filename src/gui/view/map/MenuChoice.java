package gui.view.map;

import gui.view.ViewMode;
import main.Game;
import message.Messages;
import message.Messages.MessageState;
import save.Save;
import util.PokeString;

import java.util.function.Supplier;

enum MenuChoice {
    POKEDEX(PokeString.POKEDEX, ViewMode.POKEDEX_VIEW),
    POKEMON(PokeString.POKEMON, ViewMode.PARTY_VIEW),
    BAG("Bag", mapView -> {
        Game.instance().setViewMode(ViewMode.BAG_VIEW);
        Messages.clearMessages(MessageState.BAGGIN_IT_UP);
        Messages.setMessageState(MessageState.BAGGIN_IT_UP);
    }),
    TRAINER_CARD(() -> Game.getPlayer().getName(), ViewMode.TRAINER_CARD_VIEW),
    OPTIONS("Options", ViewMode.OPTIONS_VIEW),
    SAVE("Save", mapView -> {
        // TODO: Question user if they would like to save first.
        Save.save();
        Messages.add("Your game has now been saved!");
        mapView.setState(VisualState.MESSAGE);
    }),
    EXIT("Exit", ViewMode.MAIN_MENU_VIEW), // TODO: Confirmation
    RETURN("Return", mapView -> mapView.setState(VisualState.MAP));

    private final Supplier<String> displayNameGetter;
    private final StateChanger stateChanger;

    MenuChoice(String displayName, ViewMode viewMode) {
        this(() -> displayName, viewMode);
    }

    MenuChoice(Supplier<String> displayNameGetter, ViewMode viewMode) {
        this(displayNameGetter, mapView -> Game.instance().setViewMode(viewMode));
    }

    MenuChoice(String displayName, StateChanger stateChanger) {
        this(() -> displayName, stateChanger);
    }

    MenuChoice(Supplier<String> displayNameGetter, StateChanger stateChanger) {
        this.displayNameGetter = displayNameGetter;
        this.stateChanger = stateChanger;
    }

    public String getDisplayName() {
        return this.displayNameGetter.get();
    }

    public void execute(MapView mapView) {
        this.stateChanger.execute(mapView);
    }

    @FunctionalInterface
    private interface StateChanger {
        void execute(MapView mapView);
    }
}
