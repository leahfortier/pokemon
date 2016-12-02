package gui.view.mainmenu;

import gui.Button;
import gui.view.mainmenu.VisualState.VisualStateHandler;
import util.DrawUtils;

import java.awt.Graphics;

class MainState implements VisualStateHandler {
    private static final String[] MAIN_HEADERS = { "Load Game", "New Game", "Options", "Quit" };

    private final Button[] buttons;

    MainState() {
        this.buttons = new Button[MainMenuView.NUM_MAIN_BUTTONS];
        for (int i = 0; i < buttons.length; i++) {
            this.buttons[i] = MainMenuView.createMenuButton(i);
        }
    }

    @Override
    public void draw(Graphics g, MainMenuView view) {
        DrawUtils.setFont(g, 40);
        for (int i = 0; i < this.buttons.length; i++) {
            DrawUtils.drawCenteredString(g, MAIN_HEADERS[i], this.buttons[i]);
        }
    }

    @Override
    public void update(MainMenuView view) {
        int pressed = view.getPressed(buttons);
        switch (pressed) {
            case 0: // load
                view.setVisualState(VisualState.LOAD);
                break;
            case 1: // new
                view.setVisualState(VisualState.NEW);
                break;
            case 2: // options
                view.setVisualState(VisualState.OPTIONS);
                break;
            case 3: // quit
                System.exit(0);
                break;
        }
    }

    @Override
    public Button[] getButtons() {
        return this.buttons;
    }
}
