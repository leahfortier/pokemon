package gui.view.map;

import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.layout.ArrowLayout;
import draw.layout.DrawLayout;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.PanelList;
import draw.panel.WrapPanel;
import draw.panel.WrapPanel.WrapMetrics;
import gui.TileSet;
import gui.view.map.VisualState.VisualStateHandler;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import map.Direction;
import trainer.player.medal.Medal;
import trainer.player.medal.MedalCase;
import util.FontMetrics;
import util.GeneralUtils;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class MedalCaseState implements VisualStateHandler {
    private static final List<Medal> MEDALS = Arrays.asList(Medal.values());

    private static final int MEDALS_PER_PAGE = 5;
    private static final int NUM_PAGES = GeneralUtils.getTotalPages(MEDALS.size(), MEDALS_PER_PAGE);

    private static final int NUM_BUTTONS = 2;
    private static final int RIGHT_ARROW = NUM_BUTTONS - 1;
    private static final int LEFT_ARROW = NUM_BUTTONS - 2;

    private final PanelList panels;
    private final DrawPanel[] medalPanels;
    private final DrawPanel countPanel;

    private final ButtonList buttons;
    private final Button leftButton;
    private final Button rightButton;

    private final TileSet medalTiles;

    private MedalCase medalCase;

    private int pageNum;

    public MedalCaseState() {
        int spacing = 20;
        int bottomPanelHeight = 50;
        DrawPanel medalsPanel = new DrawPanel(spacing, spacing, Point.subtract(Global.GAME_SIZE, 2*spacing, 3*spacing + bottomPanelHeight))
                .withBackgroundColor(new Color(152, 88, 240))
                .withBorderlessTransparentBackground()
                .withBlackOutline();

        countPanel = new DrawPanel(
                medalsPanel.x,
                medalsPanel.bottomY() + spacing,
                (medalsPanel.width - spacing)/2,
                bottomPanelHeight
        ).withBackgroundColor(new Color(168, 232, 72))
         .withBorderlessTransparentBackground()
         .withBlackOutline()
         .withLabelSize(24);

        DrawPanel arrowsPanel = new DrawPanel(
                countPanel.rightX() + spacing,
                countPanel.y,
                countPanel.width,
                countPanel.height
        ).withBackgroundColor(new Color(248, 120, 64))
         .withBorderlessTransparentBackground()
         .withBlackOutline();

        medalPanels = new DrawLayout(medalsPanel, MEDALS_PER_PAGE, 1, 12)
                .withDrawSetup(DrawPanel::withBlackOutline).getPanels();

        ArrowLayout arrowPanels = new ArrowLayout(arrowsPanel);
        leftButton = new Button(
                arrowPanels.getLeftPanel(),
                new ButtonTransitions().left(RIGHT_ARROW).right(RIGHT_ARROW),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, NUM_PAGES)
        ).asArrow(Direction.LEFT);

        rightButton = new Button(
                arrowPanels.getRightPanel(),
                new ButtonTransitions().left(LEFT_ARROW).right(LEFT_ARROW),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, NUM_PAGES)
        ).asArrow(Direction.RIGHT);

        this.buttons = new ButtonList(NUM_BUTTONS);
        buttons.set(LEFT_ARROW, leftButton);
        buttons.set(RIGHT_ARROW, rightButton);

        this.panels = new PanelList(medalsPanel, countPanel, arrowsPanel);

        this.medalTiles = Game.getData().getMedalTiles();
    }

    @Override
    public void draw(Graphics g, MapView mapView) {
        BasicPanels.drawCanvasPanel(g);
        panels.drawAll(g);

        List<Medal> displayMedals = GeneralUtils.pageValues(MEDALS, pageNum, MEDALS_PER_PAGE);
        for (int i = 0; i < displayMedals.size(); i++) {
            Medal medal = displayMedals.get(i);
            DrawPanel medalPanel = medalPanels[i];
            drawMedal(g, medal, medalPanel);
        }

        TextUtils.drawPageNumbers(g, 24, leftButton, rightButton, pageNum, NUM_PAGES);
        buttons.draw(g);
    }

    // Used for testing
    public WrapMetrics drawMedal(Graphics g, Medal medal) {
        return drawMedal(g, medal, medalPanels[0]);
    }

    private WrapMetrics drawMedal(Graphics g, Medal medal, DrawPanel medalPanel) {
        if (medalCase.hasMedal(medal)) {
            medalPanel.withBackgroundColor(new Color(255, 215, 0)).withBorderColor(new Color(218, 165, 32));
        } else {
            medalPanel.withBackgroundColor(Color.WHITE).withBorderColor(Color.LIGHT_GRAY);
        }
        medalPanel.drawBackground(g);

        int spacing = 10;
        int borderSize = medalPanel.getBorderSize();
        int fullSpacing = borderSize + spacing;

        int imageX = medalPanel.x + fullSpacing;
        BufferedImage medalImage = medalTiles.getTile(medalCase.hasMedal(medal) ? medal.getImageName() : Medal.getUnknownMedalImageName());
        ImageUtils.drawCenteredHeightImage(g, medalImage, imageX, medalPanel.centerY());

        FontMetrics.setFont(g, 18);
        int leftX = imageX + medalImage.getWidth() + spacing;
        int topY = medalPanel.y + fullSpacing + FontMetrics.getTextHeight(g);
        g.drawString(medal.getMedalName(), leftX, topY);

        // Threshold on the right side (red if goal not yet met)
        int rightX = medalPanel.rightX() - fullSpacing;
        g.setColor(medalCase.hasMedal(medal) ? Color.BLACK : new Color(219, 9, 46));
        TextUtils.drawRightAlignedString(g, medalCase.getThresholdString(medal), rightX, topY);

        // Width is image to border, height is text to border
        int startX = leftX - spacing;
        int startY = topY;
        WrapPanel descriptionPanel = new WrapPanel(
                startX, startY,
                medalPanel.rightX() - borderSize - startX,
                medalPanel.bottomY() - borderSize - startY,
                14
        )
                .withBorderPercentage(0)
                .withMinimumSpacing(2)
                .withStartX(leftX);

        return descriptionPanel.drawMessage(g, medal.getDescription());
    }

    @Override
    public void update(int dt, MapView mapView) {
        buttons.update();
        buttons.consumeSelectedPress();

        InputControl input = InputControl.instance();
        if (input.consumeIfDown(ControlKey.ESC) || input.consumeIfDown(ControlKey.MEDAL_CASE)) {
            mapView.setState(VisualState.MAP);
        }
    }

    @Override
    public void set(MapView mapView) {
        this.medalCase = Game.getPlayer().getMedalCase();
        this.pageNum = 0;
        this.countPanel.withLabel("Total: " + this.medalCase.numMedalsEarned() + "/" + MEDALS.size());
        this.buttons.setSelected(RIGHT_ARROW);
    }
}
