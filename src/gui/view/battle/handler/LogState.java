package gui.view.battle.handler;

import gui.Button;
import gui.ButtonHoverAction;
import gui.TileSet;
import gui.panel.DrawPanel;
import gui.view.View;
import gui.view.battle.BattleView;
import main.Game;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class LogState implements VisualStateHandler {
    private static final int LOG_LEFT_BUTTON = 0;
    private static final int LOG_RIGHT_BUTTON = 1;
    
    private static final int LOGS_PER_PAGE = 23;

    private final DrawPanel logPanel;
    private final Button[] logButtons;

    private int logPage;
    private List<String> logMessages;

    public LogState() {
        logPanel = new DrawPanel(0, 160, 417, 440)
                .withBorderPercentage(3)
                .withBlackOutline();

        logButtons = new Button[2];
        for (int i = 0; i < logButtons.length; i++) {
            logButtons[i] = new Button(
                    150 +50*i,
                    550,
                    35,
                    20,
                    ButtonHoverAction.BOX,
                    Button.getBasicTransitions(i, 1, 2)
            );
        }
    }

    @Override
    public void set(BattleView view) {
        logPage = 0;
        logMessages = Game.getPlayer().getLogMessages();

        if (logMessages.size()/LOGS_PER_PAGE > 0) {
            view.setSelectedButton(LOG_RIGHT_BUTTON);
            logButtons[LOG_RIGHT_BUTTON].setActive(true);
            view.setSelectedButton(logButtons);
        }
        else {
            logButtons[LOG_RIGHT_BUTTON].setActive(false);
        }

        logButtons[LOG_LEFT_BUTTON].setActive(false);
    }

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        logPanel.drawBackground(g);

        int start = logMessages.size() - 1 - logPage*LOGS_PER_PAGE;
        start = Math.max(0, start);

        int y = 200;
        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 12);
        for (int i = start; i >= 0 && start - i < LOGS_PER_PAGE; i--, y += 15) {
            g.drawString(logMessages.get(i), 25, y);
        }

        View.drawArrows(g, logButtons[LOG_LEFT_BUTTON], logButtons[LOG_RIGHT_BUTTON]);
        logButtons[LOG_LEFT_BUTTON].draw(g);
        logButtons[LOG_RIGHT_BUTTON].draw(g);

        // Draw Messages Box
        view.drawMenuMessagePanel(g, "Bob Loblaw's Log Blog");

        // Draw back arrow when applicable
        view.drawBackButton(g);
    }

    @Override
    public void update(BattleView view) {
        view.setSelectedButton(logButtons);

        int maxLogPage = logMessages.size()/LOGS_PER_PAGE;

        if (logButtons[LOG_LEFT_BUTTON].checkConsumePress()) {
            view.setSelectedButton(LOG_LEFT_BUTTON);
            logButtons[LOG_RIGHT_BUTTON].setForceHover(false);
            logPage = Math.max(0, logPage - 1);
        }

        if (logButtons[LOG_RIGHT_BUTTON].checkConsumePress()) {
            view.setSelectedButton(LOG_RIGHT_BUTTON);
            logButtons[LOG_LEFT_BUTTON].setForceHover(false);
            logPage = Math.min(maxLogPage, logPage + 1);
        }

        logButtons[LOG_LEFT_BUTTON].setActive(logPage > 0);
        logButtons[LOG_RIGHT_BUTTON].setActive(logPage < maxLogPage);

        if (logPage == 0 && maxLogPage > 0) {
            view.setSelectedButton(LOG_RIGHT_BUTTON);
        }
        else if (logPage == maxLogPage) {
            view.setSelectedButton(LOG_LEFT_BUTTON);
        }

        view.updateBackButton();
    }
}
