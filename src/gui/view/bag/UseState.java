package gui.view.bag;

import draw.button.Button;
import pokemon.active.PartyPokemon;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.function.Consumer;

enum UseState {
    GIVE("Give", BagView.GIVE, BagView::giveItem),
    USE("Use", BagView.USE, BagView::useItem),
    TAKE("Take", BagView.TAKE, BagView::takeItem);

    final int buttonIndex;
    private final String displayName;
    private final ApplyButton applyButton;

    private boolean clicked;

    UseState(String displayName, int buttonIndex, ApplyButton applyButton) {
        this.displayName = displayName;
        this.buttonIndex = buttonIndex;
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

    void draw(Graphics g, Button button, Color buttonColor) {
        // Highlight if selected
        if (clicked) {
            button.highlight(g, buttonColor);
        }

        // Grey out if inactive
        if (!button.isActive()) {
            button.greyOut(g);
        }

        button.fillTransparent(g);
        button.outlineTab(g, this.ordinal(), -1);
        button.label(g, 20, displayName);
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
