package gui.view.map;

import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import map.Direction;
import map.area.FlyLocation;
import util.FontMetrics;
import util.GeneralUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

class FlyState extends VisualStateHandler {
    private static final int AREAS_PER_PAGE = 3;

    private static final int NUM_BUTTONS = AREAS_PER_PAGE + 2;
    private static final int AREAS = 0;
    private static final int RIGHT_BUTTON = NUM_BUTTONS - 1;
    private static final int LEFT_BUTTON = NUM_BUTTONS - 2;

    private static final int BUTTON_PADDING = 20;

    private final DrawPanel titlePanel;
    private final ButtonList buttons;
    private final Button[] areaButtons;
    private final Button leftButton;
    private final Button rightButton;

    private List<FlyLocation> flyLocations;

    private int pageNum;

    FlyState() {
        this.titlePanel = new DrawPanel(BUTTON_PADDING, BUTTON_PADDING, 350, 60)
                .withBackgroundColor(new Color(255, 210, 86))
                .withTransparentCount(2)
                .withBorderPercentage(15)
                .withBlackOutline()
                .withLabel("Where to fly?", 30);

        int buttonHeight = (Global.GAME_SIZE.height - titlePanel.height - (AREAS_PER_PAGE + 2)*BUTTON_PADDING)/(AREAS_PER_PAGE + 1);
        areaButtons = new Button[AREAS_PER_PAGE];
        for (int i = 0; i < AREAS_PER_PAGE; i++) {
            final int index = i;
            areaButtons[i] = new Button(
                    2*BUTTON_PADDING,
                    i*buttonHeight + (i + 2)*BUTTON_PADDING + this.titlePanel.height,
                    400,
                    buttonHeight,
                    ButtonTransitions.getBasicTransitions(
                            i, AREAS_PER_PAGE, 1, 0,
                            new ButtonTransitions()
                                    .right(LEFT_BUTTON)
                                    .up(LEFT_BUTTON)
                                    .left(RIGHT_BUTTON)
                                    .down(LEFT_BUTTON)
                    ),
                    () -> {
                        FlyLocation flyLocation = this.flyLocations.get(index + pageNum*AREAS_PER_PAGE);

                        // Note: Changes view mode to map view
                        flyLocation.fly();
                    },
                    panel -> panel.skipInactive()
                                  .withBackgroundColor(new Color(68, 123, 184))
                                  .withBorderPercentage(15)
                                  .withTransparentCount(2)
                                  .withBlackOutline()
                                  .withLabelSize(25)
            );
        }

        leftButton = new Button(
                500,
                BUTTON_PADDING,
                75,
                50,
                new ButtonTransitions().right(RIGHT_BUTTON).up(AREAS_PER_PAGE - 1).left(0).down(0),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        ).asArrow(Direction.LEFT);

        rightButton = new Button(
                650,
                BUTTON_PADDING,
                75,
                50,
                new ButtonTransitions().right(0).up(AREAS_PER_PAGE - 1).left(LEFT_BUTTON).down(0),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        ).asArrow(Direction.RIGHT);

        this.buttons = new ButtonList(NUM_BUTTONS);
        buttons.set(AREAS, areaButtons);
        buttons.set(LEFT_BUTTON, leftButton);
        buttons.set(RIGHT_BUTTON, rightButton);

        this.pageNum = 0;
    }

    @Override
    public void draw(Graphics g) {
        BasicPanels.drawCanvasPanel(g);
        titlePanel.draw(g);
        buttons.drawPanels(g);

        FontMetrics.setBlackFont(g, 28);
        TextUtils.drawCenteredString(
                g,
                pageNum + 1 + "",
                (leftButton.centerX() + rightButton.centerX())/2,
                rightButton.centerY()
        );

        buttons.drawHover(g);
    }

    @Override
    public void update(int dt) {
        InputControl input = InputControl.instance();

        this.buttons.update();
        if (this.buttons.consumeSelectedPress()) {
            updateActiveButtons();
        }

        if (input.consumeIfDown(ControlKey.ESC) || input.consumeIfDown(ControlKey.FLY)) {
            view.setState(VisualState.MAP);
        }
    }

    @Override
    public void set() {
        this.flyLocations = Game.getPlayer().getFlyLocations();
        this.updateActiveButtons();
    }

    private void updateActiveButtons() {
        List<FlyLocation> locations = GeneralUtils.pageValues(flyLocations, pageNum, AREAS_PER_PAGE);
        for (int i = 0; i < areaButtons.length; i++) {
            Button button = areaButtons[i];
            button.setActiveSkip(i < locations.size());
            if (button.isActive()) {
                FlyLocation flyLocation = locations.get(i);
                button.panel().withLabel(flyLocation.getAreaName());
            }
        }
    }

    private int totalPages() {
        return GeneralUtils.getTotalPages(this.flyLocations.size(), AREAS_PER_PAGE);
    }
}
