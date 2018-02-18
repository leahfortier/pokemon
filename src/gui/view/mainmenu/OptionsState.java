package gui.view.mainmenu;

import draw.button.Button;
import draw.button.ButtonList;
import gui.view.mainmenu.VisualState.VisualStateHandler;
import sound.SoundPlayer;

import java.awt.Graphics;

class OptionsState implements VisualStateHandler {
    private static final String[] OPTIONS_HEADERS = { "Theme", "Mute", "Credits", "Return" };

    private final ButtonList buttons;

    OptionsState() {
        Button[] buttons = new Button[MainMenuView.NUM_MAIN_BUTTONS];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = MainMenuView.createMenuButton(i);
        }

        this.buttons = new ButtonList(buttons);
    }

    @Override
    public void draw(Graphics g, MainMenuView view) {
        for (int i = 0; i < this.buttons.size(); i++) {
            this.buttons.get(i).label(g, 40, OPTIONS_HEADERS[i]);
        }
    }

    @Override
    public void update(MainMenuView view) {
        buttons.update();

        int pressed = buttons.consumeSelectedPress() ? buttons.getSelected() : -1;
        switch (pressed) {
            case 0: // theme
                view.getSettings().toggleTheme();
                view.getSettings().save();
                break;
            case 1: // mute
                SoundPlayer.instance().toggleMusic();
                view.getSettings().save();
                break;
            case 2: // credits
                view.setVisualState(VisualState.CREDITS);
                break;
            case 3: // return
                view.setVisualState(VisualState.MAIN);
                break;
            default:
                break;
        }
    }

    @Override
    public ButtonList getButtons() {
        return this.buttons;
    }
}
