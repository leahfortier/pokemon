package gui.view.battle.handler;

import draw.button.ButtonList;
import gui.view.battle.BattleView;
import message.MessageUpdate;

import java.awt.Graphics;

public interface VisualStateHandler {
    ButtonList getButtons();

    void reset(BattleView view);
    void set();
    void update();
    void draw(Graphics g);

    default void checkMessage(MessageUpdate newMessage) {}
}
