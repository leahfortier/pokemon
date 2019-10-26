package gui.view.mainmenu;

import draw.button.Button;
import draw.button.ButtonList;
import gui.view.ViewMode;
import gui.view.mainmenu.VisualState.VisualStateHandler;
import main.Game;
import save.Save;

import java.awt.Graphics;

class NewSaveState implements VisualStateHandler {
    private final ButtonList buttons;
    private final Button returnButton;

    NewSaveState() {
        // Button for each save file plus return
        Button[] buttons = new Button[Save.NUM_SAVES + 1];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = MainMenuView.createMenuButton(i);
        }

        this.returnButton = buttons[Save.NUM_SAVES].setup(panel -> panel.withLabel("Return", 30));

        this.buttons = new ButtonList(buttons);
    }

    @Override
    public void draw(Graphics g, MainMenuView view) {
        for (int i = 0; i < Save.NUM_SAVES; i++) {
            view.drawSaveInformation(g, this.buttons.get(i), i, "New Save");
        }
    }

    @Override
    public void update(MainMenuView view) {
        buttons.update();
        int pressed = buttons.consumeSelectedPress() ? buttons.getSelected() : -1;

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
    public ButtonList getButtons() {
        return this.buttons;
    }
}
