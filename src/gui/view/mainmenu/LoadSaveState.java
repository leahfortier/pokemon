package gui.view.mainmenu;

import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
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
                    new int[] {
                            i, // Right
                            Button.basicTransition(i, buttons.length, 1, Direction.UP), // Up
                            i, // Left
                            i + 1 // Down
                    }
            );
        }

        Button referenceButton = MainMenuView.createMenuButton(MainMenuView.NUM_MAIN_BUTTONS - 1);

        returnButton = buttons[RETURN] = new Button(
                referenceButton.x,
                referenceButton.y,
                referenceButton.width/2 - 5,
                referenceButton.height,
                ButtonHoverAction.BOX,
                new int[] {
                        Save.NUM_SAVES + 1, // Right -- to the delete button
                        Save.NUM_SAVES - 1, // Up -- to the last save file
                        Save.NUM_SAVES + 1, // Left -- to the delete button
                        0                   // Down -- to the first save file
                }
        );

        deleteButton = buttons[DELETE] = new Button(
                referenceButton.x + referenceButton.width/2 + 5,
                returnButton.y,
                returnButton.width,
                returnButton.height,
                ButtonHoverAction.BOX,
                new int[] {
                        Save.NUM_SAVES,     // Right -- to the return button
                        Save.NUM_SAVES - 1, // Up -- to the last save file
                        Save.NUM_SAVES,     // Left -- to the return button
                        0                   // Down -- to the first save file
                }
        );

        this.buttons = new ButtonList(buttons);
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

        // Return and Delete
        returnButton.label(g, 30, "Return");
        deleteButton.label(g, 30, "Delete");
    }

    @Override
    public void update(MainMenuView view) {
        int pressed = view.getPressed(buttons);

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
