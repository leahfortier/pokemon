package gui.view.bag;

import draw.button.Button;
import item.use.MoveUseItem;
import item.use.PlayerUseItem;
import main.Game;
import pokemon.active.PartyPokemon;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.function.Consumer;

enum UseState {
    GIVE("Give", BagView.GIVE, (state, bagView, p) -> {
        Game.getPlayer().getBag().giveItem(p, bagView.selectedItem);
        state.deactivate(bagView);
    }),
    USE("Use", BagView.USE, (state, bagView, p) -> {
        if (!p.isEgg() && bagView.selectedItem.getItem() instanceof MoveUseItem) {
            bagView.selectedPokemon = p;
            bagView.state = BagState.MOVE_SELECT;
        } else {
            Game.getPlayer().getBag().usePokemonItem(bagView.selectedItem, p);
            state.deactivate(bagView);
        }
    }),
    // TODO: Change back to discard -- maybe have discard when over an item, and take when over a Pokemon
    TAKE("Take", BagView.TAKE, (state, bagView, p) -> {
        Game.getPlayer().getBag().takeItem(p);
        state.deactivate(bagView);
    });

    final int buttonIndex;
    private final String displayName;
    private final UseButton useButton;

    private boolean clicked;

    UseState(String displayName, int buttonIndex, UseButton useButton) {
        this.displayName = displayName;
        this.buttonIndex = buttonIndex;
        this.useButton = useButton;
    }

    void reset() {
        this.clicked = false;
    }

    private void deactivate(BagView bagView) {
        this.reset();

        bagView.setSelectedButton(this);
        bagView.state = BagState.ITEM_SELECT;

        if (!Game.getPlayer().getBag().hasItem(bagView.selectedItem)) {
            bagView.updateCategory();
        }

        bagView.updateActiveButtons();
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

    private void use(BagView bagView, PartyPokemon p) {
        this.useButton.useButton(this, bagView, p);
        bagView.updateActiveButtons();
    }

    // Called when the button is pressed
    void press(BagView view) {
        if (!clicked) {
            // Currently unselected  -- switch to Pokemon select to choose which pokemon to use the item with
            view.state = BagState.POKEMON_SELECT;
        } else {
            // Previously selected -- revert back to item select
            view.state = BagState.ITEM_SELECT;
        }

        clicked = !clicked;

        // When any of the state buttons are clicked, it should turn off all the other states
        forEach(otherState -> {
            if (this != otherState) {
                otherState.reset();
            }
        });

        // PlayerUseItems don't require selecting a Pokemon -- automatically use as soon as Use is pressed
        if (this == UseState.USE && view.selectedItem.getItem() instanceof PlayerUseItem) {
            Game.getPlayer().getBag().usePlayerItem(view.selectedItem);
            this.deactivate(view);
        }

        view.updateActiveButtons();
    }

    // Apply the selected state with the currently selected item and pokemon
    static void usePokemon(BagView bagView, PartyPokemon p) {
        forEach(useState -> {
            if (useState.clicked) {
                useState.use(bagView, p);
            }
        });
    }

    static void forEach(Consumer<UseState> action) {
        Arrays.stream(values()).forEach(action);
    }

    @FunctionalInterface
    private interface UseButton {
        void useButton(UseState state, BagView bagView, PartyPokemon p);
    }
}
