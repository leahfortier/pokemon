package gui.view.battle.handler;

import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.layout.ArrowLayout;
import draw.panel.DrawPanel;
import draw.panel.Panel;
import gui.view.battle.BattleView;
import main.Game;
import map.Direction;
import message.MessageUpdate;
import util.FontMetrics;
import util.GeneralUtils;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class LogState implements VisualStateHandler {
    private static final int LOGS_PER_PAGE = 23;

    private static final int NUM_BUTTONS = 2;
    private static final int RIGHT_ARROW = NUM_BUTTONS - 1;
    private static final int LEFT_ARROW = NUM_BUTTONS - 2;

    private final ButtonList buttons;
    private final Button leftButton;
    private final Button rightButton;

    private BattleView view;

    private int pageNum;
    private List<String> logMessages;

    public LogState() {
        view = Game.instance().getBattleView();

        int spacing = 15;
        int height = ArrowLayout.arrowHeight;
        Panel logPanel = view.getLargePanelSizing();
        int borderSize = view.getLargePanelBorderSize();

        ArrowLayout arrowPanels = new ArrowLayout(new DrawPanel(
                logPanel.getX() + borderSize,
                logPanel.bottomY() - borderSize - spacing - height,
                logPanel.getWidth() - 2*borderSize,
                height
        ));

        leftButton = new Button(
                arrowPanels.getLeftPanel(),
                new ButtonTransitions().left(RIGHT_ARROW).right(RIGHT_ARROW),
                () -> pressArrow(Direction.LEFT)
        ).asArrow(Direction.LEFT);

        rightButton = new Button(
                arrowPanels.getRightPanel(),
                new ButtonTransitions().left(LEFT_ARROW).right(LEFT_ARROW),
                () -> pressArrow(Direction.RIGHT)
        ).asArrow(Direction.RIGHT);

        this.buttons = new ButtonList(NUM_BUTTONS);
        buttons.set(LEFT_ARROW, leftButton);
        buttons.set(RIGHT_ARROW, rightButton);
    }

    @Override
    public void set() {}

    @Override
    public void draw(Graphics g) {
        view.drawLargeMenuPanel(g);

        FontMetrics.setBlackFont(g, 12);
        List<String> logs = GeneralUtils.pageValues(logMessages, pageNum, LOGS_PER_PAGE);
        for (int i = 0; i < logs.size(); i++) {
            g.drawString(logs.get(i), 25, 200 + i*15);
        }

        buttons.draw(g);
        TextUtils.drawPageNumbers(g, 18, leftButton, rightButton, pageNum, totalPages());

        // Draw Messages Box
        view.drawMenuMessagePanel(g, "Bob Loblaw's Log Blog");

        // Draw back arrow when applicable
        view.drawBackButton(g);
    }

    private void pressArrow(Direction direction) {
        int increment = direction.getDeltaPoint().x;
        pageNum = GeneralUtils.wrapIncrement(pageNum, increment, totalPages());
    }

    @Override
    public void update() {
        buttons.update();
        if (buttons.consumeSelectedPress()) {
            view.setVisualState();
        }

        view.updateBackButton();
    }

    private int totalPages() {
        return GeneralUtils.getTotalPages(logMessages.size(), LOGS_PER_PAGE);
    }

    @Override
    public ButtonList getButtons() {
        return buttons;
    }

    @Override
    public void reset(BattleView view) {
        this.view = view;
        this.pageNum = 0;
        this.logMessages = new ArrayList<>();
    }

    public void addLogMessage(MessageUpdate newMessage) {
        String messageString = newMessage.getMessage().trim();
        if (messageString.isEmpty()) {
            return;
        }

        logMessages.add("- " + messageString);
    }
}
