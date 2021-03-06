package gui.view.mainmenu;

import draw.button.ButtonList;
import sound.SoundPlayer;
import sound.SoundTitle;

import java.awt.Color;
import java.awt.Graphics;

enum VisualState {
    MAIN(new MainState()),
    LOAD(new LoadSaveState()),
    NEW(new NewSaveState()),
    OPTIONS(new OptionsState()),
    CREDITS(new CreditsState());

    private final VisualStateHandler visualStateHandler;

    VisualState(VisualStateHandler visualStateHandler) {
        this.visualStateHandler = visualStateHandler;
    }

    public void set() {
        ButtonList buttons = this.visualStateHandler.getButtons();
        buttons.setSelected(0);
        buttons.setFalseHover();

        this.visualStateHandler.set();

        SoundPlayer.instance().playMusic(this.getTunes());
    }

    public void update(MainMenuView view) {
        this.visualStateHandler.update(view);
    }

    public void draw(Graphics g, MainMenuView view) {
        ButtonList buttons = this.visualStateHandler.getButtons();
        Color buttonColor = view.getSettings().getTheme().getButtonColor();
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).panel().withBackgroundColor(buttonColor);
        }
        buttons.draw(g);

        this.visualStateHandler.draw(g, view);
    }

    public SoundTitle getTunes() {
        return this.visualStateHandler.getTunes();
    }

    interface VisualStateHandler {
        void draw(Graphics g, MainMenuView view);
        void update(MainMenuView view);
        ButtonList getButtons();

        default void set() {}
        default SoundTitle getTunes() {
            return SoundTitle.MAIN_MENU_TUNE;
        }
    }
}
