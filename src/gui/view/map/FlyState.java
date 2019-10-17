package gui.view.map;

import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import gui.view.map.VisualState.VisualStateHandler;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import map.Direction;
import map.area.FlyLocation;
import util.GeneralUtils;

import java.awt.Graphics;
import java.util.Iterator;
import java.util.List;

class FlyState implements VisualStateHandler {
    private static final int NUM_AREA_BUTTONS = 3;
    private static final int RIGHT_BUTTON = NUM_AREA_BUTTONS;
    private static final int LEFT_BUTTON = RIGHT_BUTTON + 1;

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
                .withBackgroundColor(null)
                .withBlackOutline();

        // Arrows and area buttons
        Button[] buttons = new Button[NUM_AREA_BUTTONS + 2];

        int buttonHeight = (Global.GAME_SIZE.height - titlePanel.height - (NUM_AREA_BUTTONS + 2)*BUTTON_PADDING)/(NUM_AREA_BUTTONS + 1);
        areaButtons = new Button[NUM_AREA_BUTTONS];
        for (int i = 0; i < NUM_AREA_BUTTONS; i++) {
            final int index = i;
            areaButtons[i] = buttons[i] = new Button(
                    2*BUTTON_PADDING,
                    i*buttonHeight + (i + 2)*BUTTON_PADDING + this.titlePanel.height,
                    400,
                    buttonHeight,
                    ButtonHoverAction.BOX,
                    ButtonTransitions.getBasicTransitions(
                            i, NUM_AREA_BUTTONS, 1, 0,
                            new ButtonTransitions()
                                    .right(LEFT_BUTTON)
                                    .up(LEFT_BUTTON)
                                    .left(RIGHT_BUTTON)
                                    .down(LEFT_BUTTON)
                    ),
                    () -> {
                        FlyLocation flyLocation = this.flyLocations.get(index + pageNum*NUM_AREA_BUTTONS);

                        // Note: Changes view mode to map view
                        flyLocation.fly();
                    }
            );
        }

        leftButton = buttons[LEFT_BUTTON] = new Button(
                500,
                BUTTON_PADDING,
                75,
                50,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(RIGHT_BUTTON).up(NUM_AREA_BUTTONS - 1).left(0).down(0),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        );

        rightButton = buttons[RIGHT_BUTTON] = new Button(
                650,
                BUTTON_PADDING,
                75,
                50,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(0).up(NUM_AREA_BUTTONS - 1).left(LEFT_BUTTON).down(0),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        );

        this.buttons = new ButtonList(buttons);
        this.pageNum = 0;
    }

    @Override
    public void draw(Graphics g, MapView mapView) {
        BasicPanels.drawCanvasPanel(g);

        titlePanel.drawBackground(g);
        titlePanel.label(g, 32, "Where to fly?");

        Iterator<FlyLocation> iter = GeneralUtils.pageIterator(flyLocations, pageNum, NUM_AREA_BUTTONS);
        for (int i = 0; i < NUM_AREA_BUTTONS && iter.hasNext(); i++) {
            FlyLocation flyLocation = iter.next();
            this.areaButtons[i].fillOutlineLabel(g, 30, flyLocation.getAreaName());
        }

        leftButton.drawArrow(g, Direction.LEFT);
        rightButton.drawArrow(g, Direction.RIGHT);
        TextUtils.drawCenteredString(
                g,
                pageNum + 1 + "",
                (leftButton.centerX() + rightButton.centerX())/2,
                (leftButton.centerY() + rightButton.centerY())/2
        );

        buttons.draw(g);
    }

    @Override
    public void update(int dt, MapView mapView) {
        InputControl input = InputControl.instance();

        this.buttons.update();
        if (this.buttons.consumeSelectedPress()) {
            updateActiveButtons();
        }

        if (input.consumeIfDown(ControlKey.ESC) || input.consumeIfDown(ControlKey.FLY)) {
            mapView.setState(VisualState.MAP);
        }
    }

    @Override
    public void set(MapView mapView) {
        this.flyLocations = Game.getPlayer().getFlyLocations();
        this.updateActiveButtons();
    }

    private void updateActiveButtons() {
        for (int i = 0; i < NUM_AREA_BUTTONS; i++) {
            areaButtons[i].setActive(i + pageNum*NUM_AREA_BUTTONS < this.flyLocations.size());
        }
    }

    private int totalPages() {
        return GeneralUtils.getTotalPages(this.flyLocations.size(), NUM_AREA_BUTTONS);
    }
}
