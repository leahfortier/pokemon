package gui.view.item;

import draw.button.ButtonPanel;
import pokemon.active.PartyPokemon;

import java.awt.Color;
import java.util.Arrays;
import java.util.function.Consumer;

enum UseState {
    GIVE("Give", BagView::giveItem),
    USE("Use", BagView::useItem),
    TAKE("Take", BagView::takeItem);

    final String displayName;
    private final ApplyButton applyButton;

    private boolean clicked;

    UseState(String displayName, ApplyButton applyButton) {
        this.displayName = displayName;
        this.applyButton = applyButton;
    }

    boolean isClicked() {
        return this.clicked;
    }

    void reset() {
        this.clicked = false;
    }

    void switchClicked() {
        this.clicked = !clicked;

        // When any of the state buttons are clicked, it should turn off all the other states
        UseState.forEach(otherState -> {
            if (this != otherState) {
                otherState.reset();
            }
        });
    }

    // Highlight if selected
    void setup(ButtonPanel panel, Color buttonColor) {
        panel.withHighlight(clicked, buttonColor);
    }

    // Apply the selected state with the currently selected item and pokemon
    static void applyPokemon(BagView bagView, PartyPokemon p) {
        forEach(useState -> {
            if (useState.clicked) {
                useState.applyButton.applyPokemon(bagView, p);
            }
        });
    }

    static void forEach(Consumer<UseState> action) {
        Arrays.stream(values()).forEach(action);
    }

    @FunctionalInterface
    private interface ApplyButton {
        // What this state should do when a Pokemon is selected while it is clicked
        void applyPokemon(BagView bagView, PartyPokemon p);
    }
}
