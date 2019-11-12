package gui.view.battle.handler;

import draw.button.ButtonList;
import gui.view.battle.BattleView;
import main.Game;
import message.MessageUpdate;

import java.awt.Graphics;

public abstract class VisualStateHandler {
    protected BattleView view;

    public VisualStateHandler() {
        view = Game.instance().getBattleView();
    }

    public final void setBattle(BattleView view) {
        this.view = view;
        this.reset();
    }

    // Called when a new battle is started -- to be overridden as necessary
    protected void reset() {}

    // Called when this state is entered from a different state
    public final void movedToFront() {
        this.getButtons().setSelected(0);
        this.set();
    }

    // Used whenever this state should be updated (like an updateActiveButtons kind of thing)
    // Called when moved to front, when buttons are pressed, and other update-like situations
    public void set() {}

    // Default update method -- check buttons and back button
    // Okay for this to be overridden if follows a different format
    public void update() {
        ButtonList buttons = this.getButtons();
        buttons.update();
        if (buttons.consumeSelectedPress()) {
            view.setVisualState();
        }

        // Go back to main menu if applicable
        if (this.includeBackButton()) {
            view.updateBackButton();
        }
    }

    // By default, states have a back button to return to the main menu
    protected boolean includeBackButton() {
        return true;
    }

    protected abstract ButtonList getButtons();
    public abstract void draw(Graphics g);

    public void checkMessage(MessageUpdate newMessage) {}
}
