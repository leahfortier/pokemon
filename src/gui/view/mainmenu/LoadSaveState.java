package gui.view.mainmenu;

import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonPanel.ButtonPanelSetup;
import draw.button.ButtonTransitions;
import gui.view.mainmenu.VisualState.VisualStateHandler;
import map.Direction;
import save.Save;

import java.awt.Graphics;

class LoadSaveState implements VisualStateHandler {
    private static final int RETURN = Save.NUM_SAVES;
    private static final int DELETE = RETURN + 1;

    private final ButtonList buttons;
    private final Button returnButton;
    private final Button deleteButton;

    private boolean deletePressed;

    LoadSaveState() {
        Button[] buttons = new Button[Save.NUM_SAVES + 2];
        for (int i = 0; i < Save.NUM_SAVES; i++) {
            buttons[i] = MainMenuView.createMenuButton(
                    i,
                    new ButtonTransitions()
                            .right(i)
                            .left(i)
                            .down(i + 1)
                            .basic(Direction.UP, i, buttons.length, 1)
            );
        }

        Button referenceButton = MainMenuView.createMenuButton(MainMenuView.NUM_MAIN_BUTTONS - 1);
        int spacing = 10;
        int newWidth = (referenceButton.width - spacing)/2;

        returnButton = buttons[RETURN] = new Button(
                referenceButton.x,
                referenceButton.y,
                newWidth,
                referenceButton.height,
                new ButtonTransitions().right(DELETE).up(Save.NUM_SAVES - 1).left(DELETE).down(0),
                () -> {}, // Handled in update
                halfButtonSetup("Return")
        );

        deleteButton = buttons[DELETE] = new Button(
                returnButton.rightX() + spacing,
                returnButton.y,
                returnButton.width,
                returnButton.height,
                new ButtonTransitions().right(RETURN).up(Save.NUM_SAVES - 1).left(RETURN).down(0),
                () -> {}, // Handled in update
                halfButtonSetup("Delete")
        );

        this.buttons = new ButtonList(buttons);
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
        for (int i = 0; i < Save.NUM_SAVES; i++) {
            view.drawSaveInformation(g, this.buttons.get(i), i, "Empty");
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
