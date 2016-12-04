package gui.view.battle.handler;

import gui.TileSet;
import gui.view.battle.BattleView;
import message.MessageUpdate;

import java.awt.Graphics;

public interface VisualStateHandler {
    void set(BattleView view);
    void update(BattleView view);
    void draw(BattleView view, Graphics g, TileSet tiles);

    default void reset() {}
    default void checkMessage(MessageUpdate newMessage) {}
}
