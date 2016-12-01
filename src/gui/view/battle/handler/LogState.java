package gui.view.battle.handler;

import gui.Button;
import gui.TileSet;
import gui.view.View;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import input.ControlKey;
import util.DrawUtils;

import java.awt.Color;
import java.awt.Graphics;

public class LogState implements VisualStateHandler {

    @Override
    public void set(BattleView view) {}

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        g.drawImage(tiles.getTile(0x10), 0, 160, null);

        int start = view.logMessages.size() - 1 - view.logPage * BattleView.LOGS_PER_PAGE;
        start = Math.max(0, start);

        int y = 200;
        g.setColor(Color.BLACK);
        DrawUtils.setFont(g, 12);
        for (int i = start; i >= 0 && start - i < BattleView.LOGS_PER_PAGE; i--, y += 15) {
            g.drawString(view.logMessages.get(i), 25, y);
        }

        View.drawArrows(g, view.logLeftButton, view.logRightButton);
        view.logLeftButton.draw(g);
        view.logRightButton.draw(g);

        // Draw Messages Box
        g.drawImage(tiles.getTile(0x20), 415, 440, null);

        g.setColor(Color.BLACK);
        DrawUtils.setFont(g, 40);
        g.drawString("Bob Loblaw's", 440, 500);
        g.drawString("Log Blog", 440, 550);

        // Draw back arrow when applicable
        View.drawArrows(g, null, view.backButton);
        view.backButton.draw(g);
    }

    @Override
    public void update(BattleView view) {
        view.selectedButton = Button.update(view.logButtons, view.selectedButton);
        view.backButton.update(false, ControlKey.BACK);

        int maxLogPage = view.logMessages.size()/BattleView.LOGS_PER_PAGE;

        if (view.logLeftButton.checkConsumePress()) {
            view.selectedButton = BattleView.LOG_LEFT_BUTTON;
            view.logRightButton.setForceHover(false);
            view.logPage = Math.max(0, view.logPage - 1);
        }

        if (view.logRightButton.checkConsumePress()) {
            view.selectedButton = BattleView.LOG_RIGHT_BUTTON;
            view.logLeftButton.setForceHover(false);
            view.logPage = Math.min(maxLogPage, view.logPage + 1);
        }

        view.logLeftButton.setActive(view.logPage > 0);
        view.logRightButton.setActive(view.logPage < maxLogPage);

        if (view.logPage == 0 && maxLogPage > 0) {
            view.selectedButton = BattleView.LOG_RIGHT_BUTTON;
        }
        else if (view.logPage == maxLogPage) {
            view.selectedButton = BattleView.LOG_LEFT_BUTTON;
        }

        if (view.backButton.checkConsumePress()) {
            view.setVisualState(VisualState.MENU);
        }
    }
}
