package gui.view.mainmenu;

import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonPanel.ButtonPanelSetup;
import draw.button.ButtonTransitions;
import gui.view.mainmenu.VisualState.VisualStateHandler;
import save.Save;

import java.awt.Graphics;

class LoadSaveState implements VisualStateHandler {
    private static final int NUM_BUTTONS = Save.NUM_SAVES + 2;
    private static final int SAVES = 0;
    private static final int BOTTOM_SAVE = SAVES + Save.NUM_SAVES - 1;
    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int DELETE = NUM_BUTTONS - 2;

    private final ButtonList buttons;
    private final Button[] saveButtons;

    private boolean deletePressed;

    LoadSaveState() {
        saveButtons = new Button[Save.NUM_SAVES];
        for (int i = 0; i < saveButtons.length; i++) {
            saveButtons[i] = MainMenuView.createMenuButton(
                    i,
                    ButtonTransitions.getBasicTransitions(
                            i, saveButtons.length, 1, SAVES,
                            new ButtonTransitions().up(RETURN).down(DELETE)
                    )
            );
        }

        Button referenceButton = MainMenuView.createMenuButton(MainMenuView.NUM_MAIN_BUTTONS - 1);
        int spacing = 10;
        int newWidth = (referenceButton.width - spacing)/2;

        // Handled in update
        Button returnButton = new Button(
                referenceButton.x,
                referenceButton.y,
                newWidth,
                referenceButton.height,
                new ButtonTransitions().right(DELETE).up(BOTTOM_SAVE).left(DELETE).down(SAVES),
                () -> {}, // Handled in update
                halfButtonSetup("Return")
        );

        // Handled in update
        Button deleteButton = new Button(
                returnButton.rightX() + spacing,
                returnButton.y,
                returnButton.width,
                returnButton.height,
                new ButtonTransitions().right(RETURN).up(BOTTOM_SAVE).left(RETURN).down(SAVES),
                () -> {}, // Handled in update
                halfButtonSetup("Delete")
        );

        this.buttons = new ButtonList(NUM_BUTTONS);
        buttons.set(SAVES, saveButtons);
        buttons.set(DELETE, deleteButton);
        buttons.set(RETURN, returnButton);
    }

    private ButtonPanelSetup halfButtonSetup(String label) {
        return panel -> panel.withTransparentCount(2)
                             .withBorderPercentage(15)
                             .withBlackOutline()
                             .withLabel(label, 30);
    }

    @Override
    public void set() {
        deletePressed = false;
    }

    @Override
    public void draw(Graphics g, MainMenuView view) {
        // Draw each save information button
        for (int i = 0; i < saveButtons.length; i++) {
            view.drawSaveInformation(g, saveButtons[i], i, "Empty");
        }
    }

    @Override
    public void update(MainMenuView view) {
        buttons.update();

        int pressed = buttons.consumeSelectedPress() ? buttons.getSelected() : -1;

        // Load Save File
        if (pressed >= 0 && pressed < Save.NUM_SAVES) {
            if (view.hasSavedInfo(pressed)) {
                if (deletePressed) {
                    deletePressed = false;
                    Save.deleteSave(pressed); // TODO: ask to delete first
                    view.reloadSaveInfo();
                } else {
                    view.loadSave(pressed);
                }
            }
        }
        // Return
        else if (pressed == RETURN) {
            view.setVisualState(VisualState.MAIN);
        }
        // Delete
        else if (pressed == DELETE) {
            deletePressed = true;
        }
    }

    @Override
    public ButtonList getButtons() {
        return this.buttons;
    }
}
