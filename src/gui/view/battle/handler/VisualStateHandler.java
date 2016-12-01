package gui.view.battle.handler;

import gui.TileSet;
import gui.view.battle.BattleView;
import message.MessageUpdate;

import java.awt.Graphics;

public interface VisualStateHandler {
    void update(BattleView view);
    void set(BattleView view); // TODO: Default
    void draw(BattleView view, Graphics g, TileSet tiles);
    default void reset() {}
    default void checkMessage(MessageUpdate newMessage) {}
}
