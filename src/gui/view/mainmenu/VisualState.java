package gui.view.mainmenu;

import draw.button.Button;
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
    
    interface VisualStateHandler {
        void draw(Graphics g, MainMenuView view);
        void update(MainMenuView view);
        Button[] getButtons();
        
        default void set() {}
        
        default SoundTitle getTunes() {
            return SoundTitle.MAIN_MENU_TUNE;
        }
    }
    
    public void set() {
        this.visualStateHandler.set();
    }
    
    public void update(MainMenuView view) {
        this.visualStateHandler.update(view);
    }
    
    public void draw(Graphics g, MainMenuView view) {
        this.visualStateHandler.draw(g, view);
    }
    
    public Button[] getButtons() {
        return this.visualStateHandler.getButtons();
    }
    
    public SoundTitle getTunes() {
        return this.visualStateHandler.getTunes();
    }
}
