package gui.view.battle.handler;

import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import gui.view.battle.BattleView;
import main.Game;
import map.Direction;
import util.FontMetrics;
import util.GeneralUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.List;

public class LogState implements VisualStateHandler {
    private static final int LOGS_PER_PAGE = 23;

    private final ButtonList logButtons;
    private final Button leftArrow;
    private final Button rightArrow;

    private int logPage;
    private List<String> logMessages;

    public LogState() {
        Button[] logButtons = new Button[2];
        for (int i = 0; i < logButtons.length; i++) {
            logButtons[i] = new Button(
                    150 + 50*i,
                    550,
                    35,
                    20,
                    ButtonHoverAction.BOX,
                    ButtonTransitions.getBasicTransitions(i, 1, 2)
            );
        }

        this.logButtons = new ButtonList(logButtons);
        this.leftArrow = logButtons[0];
        this.rightArrow = logButtons[1];
    }

    @Override
    public void set(BattleView view) {
        logPage = 0;
        logMessages = Game.getPlayer().getLogMessages();
    }

    @Override
    public void draw(BattleView view, Graphics g) {
        view.drawLargeMenuPanel(g);

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 12);

        Iterator<String> logIter = GeneralUtils.pageIterator(logMessages, logPage, LOGS_PER_PAGE);
        for (int i = 0; i < LOGS_PER_PAGE && logIter.hasNext(); i++) {
            g.drawString(logIter.next(), 25, 200 + i*15);
        }

        leftArrow.drawArrow(g, Direction.LEFT);
        rightArrow.drawArrow(g, Direction.RIGHT);

        logButtons.draw(g);

        // Draw Messages Box
        view.drawMenuMessagePanel(g, "Bob Loblaw's Log Blog");

        // Draw back arrow when applicable
        view.drawBackButton(g);
    }

    @Override
    public void update(BattleView view) {
        logButtons.update();

        int increment = 0;
        if (leftArrow.checkConsumePress()) {
            increment = -1;
        }
        if (rightArrow.checkConsumePress()) {
            increment = 1;
        }

        int totalPages = totalPages();
        if (increment != 0) {
            logPage = GeneralUtils.wrapIncrement(logPage, increment, totalPages);
        }

        view.updateBackButton();
    }

    private int totalPages() {
        return GeneralUtils.getTotalPages(logMessages.size(), LOGS_PER_PAGE);
    }

    @Override
    public ButtonList getButtons() {
        return logButtons;
    }
}
