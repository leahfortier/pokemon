package gui.view.mainmenu;

import draw.button.Button;
import draw.button.ButtonList;
import gui.view.mainmenu.VisualState.VisualStateHandler;

import java.awt.Graphics;

class MainState implements VisualStateHandler {
    private static final String[] MAIN_HEADERS = { "Load Game", "New Game", "Options", "Quit" };

    private final ButtonList buttons;

    MainState() {
        Button[] buttons = new Button[MainMenuView.NUM_MAIN_BUTTONS];
        for (int i = 0; i < buttons.length; i++) {
            String header = MAIN_HEADERS[i];
            buttons[i] = MainMenuView.createMenuButton(i)
                                     .setup(panel -> panel.withLabel(header));
        }

        this.buttons = new ButtonList(buttons);
    }

    @Override
    public void draw(Graphics g, MainMenuView view) {}

    @Override
    public void update(MainMenuView view) {
        buttons.update();

        int pressed = buttons.consumeSelectedPress() ? buttons.getSelected() : -1;
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
    public ButtonList getButtons() {
        return this.buttons;
    }
}
