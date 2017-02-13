package gui.view.mainmenu;

import draw.button.Button;
import gui.view.ViewMode;
import gui.view.mainmenu.VisualState.VisualStateHandler;
import main.Game;
import util.Save;

import java.awt.Graphics;

class NewSaveState implements VisualStateHandler {
    private final Button[] buttons;

    NewSaveState() {
        // Button for each save file plus return
        this.buttons = new Button[Save.NUM_SAVES + 1];
        for (int i = 0; i < this.buttons.length; i++) {
            this.buttons[i] = MainMenuView.createMenuButton(i);
        }
    }

    @Override
    public void draw(Graphics g, MainMenuView view) {
        for (int i = 0; i < Save.NUM_SAVES; i++) {
            view.drawSaveInformation(g, this.buttons[i], i, "New Save");
        }

        this.buttons[Save.NUM_SAVES].label(g, 40, "Return");
    }

    @Override
    public void update(MainMenuView view) {
        int pressed = view.getPressed(buttons);

        // Load Save File
        if (pressed >= 0 && pressed < Save.NUM_SAVES) {
            // TODO: Ask to delete
            Game.instance().newSave(pressed);
            Game.instance().setViewMode(ViewMode.START_VIEW);
        }
        // Return
        else if (pressed == Save.NUM_SAVES) {
            view.setVisualState(VisualState.MAIN);
        }
    }

    @Override
    public Button[] getButtons() {
        return this.buttons;
    }
}
