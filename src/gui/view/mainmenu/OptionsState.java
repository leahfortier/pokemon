package gui.view.mainmenu;

import gui.Button;
import gui.view.mainmenu.VisualState.VisualStateHandler;
import sound.SoundPlayer;
import util.DrawUtils;

import java.awt.Graphics;

class OptionsState implements VisualStateHandler {
    private static final String[] OPTIONS_HEADERS = { "Theme", "Mute", "Credits", "Return" };

    private final Button[] buttons;

    OptionsState() {
        this.buttons = new Button[MainMenuView.NUM_MAIN_BUTTONS];
        for (int i = 0; i < buttons.length; i++) {
            this.buttons[i] = MainMenuView.createMenuButton(i);
        }
    }

    @Override
    public void draw(Graphics g, MainMenuView view) {
        DrawUtils.setFont(g, 40);
        for (int i = 0; i < this.buttons.length; i++) {
            DrawUtils.drawCenteredString(g, OPTIONS_HEADERS[i], this.buttons[i]);
        }
    }

    @Override
    public void update(MainMenuView view) {
        int pressed = view.getPressed(buttons);

        switch (pressed) {
            case 0: // theme
                view.toggleTheme();
                view.saveSettings();
                break;
            case 1: // mute
                SoundPlayer.soundPlayer.toggleMusic();
                view.saveSettings();
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
    public Button[] getButtons() {
        return this.buttons;
    }
}
