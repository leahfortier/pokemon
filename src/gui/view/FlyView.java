package gui.view;

import draw.Alignment;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.panel.BasicPanels;
import draw.button.panel.DrawPanel;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import map.AreaData;
import map.Direction;
import trainer.CharacterData;
import util.GeneralUtils;

import java.awt.Graphics;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

class FlyView extends View {
    private static final int NUM_AREA_BUTTONS = 3;
    private static final int RIGHT_BUTTON = NUM_AREA_BUTTONS;
    private static final int LEFT_BUTTON = RIGHT_BUTTON + 1;

    private static final int BUTTON_PADDING = 20;

    private final DrawPanel titlePanel;
    private final Button[] buttons;

    private List<Entry<String, String>> flyLocations;

    private int selectedButton;
    private int pageNum;

    FlyView() {
        this.titlePanel = new DrawPanel(BUTTON_PADDING, BUTTON_PADDING, 350, 60)
                .withBackgroundColor(null)
                .withBlackOutline();

        // Arrows and area buttons
        this.buttons = new Button[NUM_AREA_BUTTONS + 2];

        int buttonHeight = (Global.GAME_SIZE.height - titlePanel.height - (NUM_AREA_BUTTONS + 2)*BUTTON_PADDING)/(NUM_AREA_BUTTONS + 1);
        for (int i = 0; i < NUM_AREA_BUTTONS; i++) {
            this.buttons[i] = new Button(
                    2*BUTTON_PADDING,
                    i*buttonHeight + (i + 2)*BUTTON_PADDING + this.titlePanel.height,
                    400,
                    buttonHeight,
                    ButtonHoverAction.BOX,
                    Button.getBasicTransitions(i, NUM_AREA_BUTTONS, 1, 0, new int[] { LEFT_BUTTON, LEFT_BUTTON, RIGHT_BUTTON, -1 })
            );
        }

        this.buttons[LEFT_BUTTON] = new Button(
                500,
                BUTTON_PADDING,
                75,
                50,
                ButtonHoverAction.BOX,
                new int[] { RIGHT_BUTTON, NUM_AREA_BUTTONS - 1, 0, 0 }
        );

        this.buttons[RIGHT_BUTTON] = new Button(
                650,
                BUTTON_PADDING,
                75,
                50,
                ButtonHoverAction.BOX,
                new int[] { 0, NUM_AREA_BUTTONS - 1, LEFT_BUTTON, 0 }
        );

        this.pageNum = 0;
    }

    @Override
    public void update(int dt) {
        InputControl input = InputControl.instance();

        selectedButton = Button.update(this.buttons, selectedButton);

        if (this.buttons[selectedButton].checkConsumePress()) {
            if (selectedButton < NUM_AREA_BUTTONS) {
                CharacterData player = Game.getPlayer();
                Entry<String, String> entry = this.flyLocations.get(selectedButton + pageNum*NUM_AREA_BUTTONS);

                String mapName = entry.getKey();
                String areaName = entry.getValue();

                AreaData area = Game.getData().getMap(mapName).getArea(areaName);
                if (!area.isFlyLocation()) {
                    Global.error(mapName + " is not a valid fly location.");
                }

                String flyEntrance = area.getFlyLocation();

                player.setMap(mapName, flyEntrance);
                player.setDirection(Direction.DOWN);
                player.setMapReset(true);

                Game.instance().setViewMode(ViewMode.MAP_VIEW);
            }
            else if (selectedButton == LEFT_BUTTON) {
                pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages());
            }
            else if (selectedButton == RIGHT_BUTTON) {
                pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages());
            }

            updateActiveButtons();
        }

        if (input.consumeIfDown(ControlKey.ESC)) {
            Game.instance().popView();
        }
    }

    private void updateActiveButtons() {
        for (int i = 0; i < NUM_AREA_BUTTONS; i++) {
            buttons[i].setActive(i + pageNum*NUM_AREA_BUTTONS < this.flyLocations.size());
        }
    }

    @Override
    public void draw(Graphics g) {
        BasicPanels.drawCanvasPanel(g);

        titlePanel.drawBackground(g);
        titlePanel.label(g, 32, "Where to fly?");

        Iterator<Entry<String, String>> iter = GeneralUtils.pageIterator(this.flyLocations, pageNum, NUM_AREA_BUTTONS);
        for (int i = 0; i < NUM_AREA_BUTTONS && iter.hasNext(); i++) {
            Entry<String, String> entry = iter.next();

            Button locationButton = this.buttons[i];
            locationButton.fillTransparent(g);
            locationButton.blackOutline(g);
            locationButton.label(g, 30, entry.getValue());
        }

        Button leftButton = this.buttons[LEFT_BUTTON];
        Button rightButton = this.buttons[RIGHT_BUTTON];
        leftButton.drawArrow(g, Direction.LEFT);
        rightButton.drawArrow(g, Direction.RIGHT);
        Alignment.drawCenteredString(
                g,
                pageNum + 1 + "",
                (leftButton.centerX() + rightButton.centerX())/2,
                (leftButton.centerY() + rightButton.centerY())/2
        );

        for (Button button : buttons) {
            button.draw(g);
        }
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.FLY_VIEW;
    }

    @Override
    public void movedToFront() {
        this.flyLocations = Game.getPlayer().getFlyLocations();
        this.updateActiveButtons();
    }

    private int totalPages() {
        return this.flyLocations.size()/NUM_AREA_BUTTONS + 1;
    }
}
