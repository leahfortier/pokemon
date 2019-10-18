package gui.view.mainmenu;

import draw.button.ButtonList;
import sound.SoundPlayer;
import sound.SoundTitle;

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
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).fillBordered(g, view.getSettings().getTheme().getButtonColor());
        }
        buttons.drawHover(g);

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
