package gui.view.bag;

import draw.button.Button;
import item.use.MoveUseItem;
import item.use.PlayerUseItem;
import main.Game;
import pokemon.ActivePokemon;

import java.awt.Graphics;

enum UseState {
    GIVE("Give", BagView.GIVE, (state, bagView, p) -> {
        Game.getPlayer().getBag().giveItem(p, bagView.selectedItem);
        state.deactivate(bagView);
    }),
    USE("Use", BagView.USE, (state, bagView, p) -> {
        if (bagView.selectedItem.getItem() instanceof MoveUseItem) {
            bagView.selectedPokemon = p;
            bagView.state = BagState.MOVE_SELECT;
        } else {
            Game.getPlayer().getBag().useItem(bagView.selectedItem, p);
            state.deactivate(bagView);
        }
    }),
    // TODO: Change back to discard -- maybe have discard when over an item, and take when over a Pokemon
    TAKE("Take", BagView.TAKE, (state, bagView, p) -> {
        Game.getPlayer().getBag().takeItem(p);
        state.deactivate(bagView);
    });

    private final String displayName;
    final int buttonIndex;
    private final UseButton useButton;

    private boolean clicked;

    UseState(String displayName, int buttonIndex, UseButton useButton) {
        this.displayName = displayName;
        this.buttonIndex = buttonIndex;
        this.useButton = useButton;
    }

    private void deactivate(BagView bagView) {
        this.clicked = false;
        bagView.setSelectedButton(this);
        bagView.state = BagState.ITEM_SELECT;

        if (!Game.getPlayer().getBag().hasItem(bagView.selectedItem)) {
            bagView.updateCategory();
        }

        bagView.updateActiveButtons();
    }

    @FunctionalInterface
    private interface UseButton {
        void useButton(UseState state, BagView bagView, ActivePokemon p);
    }

    void draw(Graphics g, Button button) {
        // Grey out selected buttons
        if (clicked) {
            button.greyOut(g, false);
        }

        // Grey out inactive buttons
        if (!button.isActive()) {
            button.greyOut(g, true);
        }

        button.fillTransparent(g);
        button.outlineTab(g, this.ordinal(), -1);
        button.label(g, 20, displayName);
    }

    void use(BagView bagView, ActivePokemon p) {
        if (this.clicked) {
            this.useButton.useButton(this, bagView, p);
            bagView.updateActiveButtons();
        }
    }

    void update(BagView view) {
        if (!clicked) {
            view.state = BagState.POKEMON_SELECT;
        } else {
            view.state = BagState.ITEM_SELECT;
        }

        clicked = !clicked;

        for (UseState otherState : UseState.values()) {
            if (this == otherState) {
                continue;
            }

            otherState.clicked = false;
        }

        if (this == UseState.USE && view.selectedItem.getItem() instanceof PlayerUseItem) {
            Game.getPlayer().getBag().useItem(view.selectedItem);
        }

        view.updateActiveButtons();
    }
}
