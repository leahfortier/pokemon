package gui.view.map;

import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MedalCaseState implements VisualStateHandler {
    private static final List<Medal> MEDALS = Arrays.stream(Medal.values()).collect(Collectors.toList());

    private static final int NUM_MEDAL_PANELS = 5;
    private static final int NUM_PAGES = (int)Math.ceil((double)MEDALS.size()/NUM_MEDAL_PANELS);

    private static final int NUM_BUTTONS = 2;
    private static final int RIGHT_ARROW = NUM_BUTTONS - 1;
    private static final int LEFT_ARROW = NUM_BUTTONS - 2;

    private final DrawPanel[] medalPanels;

    private final ButtonList buttons;
    private final Button leftButton;
    private final Button rightButton;

    private final TileSet medalTiles;

    private MedalCase medalCase;

    private int pageNum;

    MedalCaseState() {
        int spacing = 20;

        int panelWidth = Global.GAME_SIZE.width - 2*spacing;
        int panelHeight = (Global.GAME_SIZE.height - (NUM_MEDAL_PANELS + 2)*spacing)/(NUM_MEDAL_PANELS + 1);

        medalPanels = new DrawPanel[NUM_MEDAL_PANELS];
        for (int i = 0; i < medalPanels.length; i++) {
            this.medalPanels[i] = new DrawPanel(
                    spacing,
                    spacing*(i + 1) + panelHeight*i,
                    panelWidth,
                    panelHeight
            ).withBlackOutline();
        }

        int arrowWidth = 35;
        int arrowHeight = 20;
        int arrowSpacing = 70;

        leftButton = new Button(
                medalPanels[0].centerX() - arrowSpacing - arrowWidth,
                medalPanels[medalPanels.length - 1].centerY() + spacing + medalPanels[0].height,
                arrowWidth,
                arrowHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions().left(RIGHT_ARROW).right(RIGHT_ARROW),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, NUM_PAGES)
        ).asArrow(Direction.LEFT);

        rightButton = new Button(
                medalPanels[0].centerX() + arrowSpacing,
                leftButton.y,
                arrowWidth,
                arrowHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions().left(LEFT_ARROW).right(LEFT_ARROW),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, NUM_PAGES)
        ).asArrow(Direction.RIGHT);

        this.buttons = new ButtonList(NUM_BUTTONS);
        buttons.set(LEFT_ARROW, leftButton);
        buttons.set(RIGHT_ARROW, rightButton);

        this.medalTiles = Game.getData().getMedalTiles();
    }

    @Override
    public void draw(Graphics g, MapView mapView) {
        BasicPanels.drawCanvasPanel(g);

        List<Medal> displayMedals = GeneralUtils.pageValues(MEDALS, pageNum, NUM_MEDAL_PANELS);
        for (int i = 0; i < displayMedals.size(); i++) {
            Medal medal = displayMedals.get(i);

            DrawPanel medalPanel = medalPanels[i];
            if (medalCase.hasMedal(medal)) {
                medalPanel.withBackgroundColor(new Color(255, 215, 0)).withBorderColor(new Color(218, 165, 32));
            } else {
                medalPanel.withBackgroundColor(Color.WHITE).withBorderColor(Color.LIGHT_GRAY);
            }
            medalPanel.drawBackground(g);

            int spacing = 10;
            int imageX = medalPanel.x + medalPanel.getBorderSize() + spacing;
            BufferedImage medalImage = medalTiles.getTile(medalCase.hasMedal(medal) ? medal.getImageName() : Medal.getUnknownMedalImageName());
            ImageUtils.drawCenteredHeightImage(g, medalImage, imageX, medalPanel.centerY());

            int leftX = imageX + medalImage.getWidth() + spacing;
            int topY = medalPanel.y + medalPanel.getBorderSize() + spacing + FontMetrics.getTextHeight(g);
            FontMetrics.setFont(g, 20);
            g.drawString(medal.getMedalName(), leftX, topY);

            if (medal.hasThreshold()) {
                g.setColor(medalCase.hasMedal(medal) ? Color.BLACK : new Color(219, 9, 46));
                TextUtils.drawRightAlignedString(g, medalCase.getCount(medal) + "/" + medal.getThreshold(), medalPanel.rightX() - medalPanel.getBorderSize() - spacing, topY);
            }

            FontMetrics.setBlackFont(g, 16);
            TextUtils.drawWrappedText(
                    g,
                    medal.getDescription(),
                    leftX,
                    medalPanel.bottomY() - medalPanel.getBorderSize() - 2*spacing,
                    medalPanel.rightX() - leftX - medalPanel.getBorderSize() - spacing
            );
        }

        TextUtils.drawPageNumbers(g, 30, leftButton, rightButton, pageNum, NUM_PAGES);

        buttons.draw(g);
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
    }
}
