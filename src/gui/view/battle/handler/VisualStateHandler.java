package gui.view.battle.handler;

import gui.TileSet;
import gui.view.battle.BattleView;

import java.awt.Graphics;

public interface VisualStateHandler {
    void update(BattleView view);
    void set(BattleView view);
    void draw(BattleView view, Graphics g, TileSet tiles);
}
