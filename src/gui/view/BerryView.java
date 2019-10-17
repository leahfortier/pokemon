package gui.view;

import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import gui.TileSet;
import gui.view.item.BagLayout;
import input.ControlKey;
import input.InputControl;
import item.ItemNamesies;
import item.bag.Bag;
import item.bag.BagCategory;
import item.berry.farm.BerryFarm;
import item.berry.farm.PlantedBerry;
import main.Game;
import map.Direction;
import util.GeneralUtils;
import util.string.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Set;

public class BerryView extends View {
    private static final Color BACKGROUND_COLOR = BagCategory.BERRY.getColor();

    private static final int ITEMS_PER_PAGE = 10;

    private static final int NUM_COLS = 4;
    private static final int NUM_ROWS = BerryFarm.MAX_BERRIES/NUM_COLS;

    private static final int NUM_BUTTONS = ITEMS_PER_PAGE + 4;
    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int HARVEST = NUM_BUTTONS - 2;
    private static final int RIGHT_ARROW = NUM_BUTTONS - 3;
    private static final int LEFT_ARROW = NUM_BUTTONS - 4;

    private final BagLayout layout;

    private final DrawPanel tabPanel;
    private final DrawPanel[] berryPanels;

    private final ButtonList buttons;
    private final Button[] itemButtons;
    private final Button harvestButton;
    private final Button returnButton;
    private final Button leftArrow;
    private final Button rightArrow;

    private int pageNum;
    private String message;
    private ItemNamesies selectedItem;

    private BerryFarm berryFarm;

    BerryView() {
        this.layout = new BagLayout(true);

        layout.bagPanel.withBackgroundColor(BACKGROUND_COLOR)
                       .withBlackOutline();

        tabPanel = layout.tabPanels[BagCategory.BERRY.ordinal()]
                .withBackgroundColor(BACKGROUND_COLOR)
                .withTransparentBackground()
                .withBorderPercentage(0)
                .withMissingBlackOutline(Direction.DOWN);

        returnButton = new Button(
                layout.returnPanel,
                new ButtonTransitions().up(RIGHT_ARROW).down(RIGHT_ARROW),
                ButtonPressAction.getExitAction()
        );

        Button[] berryButtons = layout.leftPanel.getButtons(10, NUM_ROWS, NUM_COLS);
        berryPanels = new DrawPanel[NUM_ROWS*NUM_COLS];
        for (int i = 0; i < berryPanels.length; i++) {
            berryPanels[i] = new DrawPanel(berryButtons[i]).withBlackOutline().withFullTransparency();
        }

        selectedItem = ItemNamesies.NO_ITEM;
        itemButtons = layout.getItemButtons(
                0,
                new ButtonTransitions().up(HARVEST).down(RIGHT_ARROW),
                index -> selectedItem = GeneralUtils.getPageValue(this.getDisplayBerries(), pageNum, ITEMS_PER_PAGE, index)
        );

        // Harvest button is all the selected buttons at once
        harvestButton = new Button(
                layout.selectedPanel.x,
                layout.selectedButtonPanels[0].y,
                layout.selectedPanel.width,
                layout.selectedButtonPanels[0].height,
                ButtonHoverAction.BOX,
                new ButtonTransitions().up(RETURN).down(0),
                () -> message = berryFarm.harvest(selectedItem)
        );

        leftArrow = new Button(
                layout.leftArrow,
                new ButtonTransitions().right(RIGHT_ARROW).up(ITEMS_PER_PAGE - 2).left(RIGHT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        );

        rightArrow = new Button(
                layout.rightArrow,
                new ButtonTransitions().right(LEFT_ARROW).up(ITEMS_PER_PAGE - 1).left(LEFT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        );

        Button[] buttons = new Button[NUM_BUTTONS];
        System.arraycopy(itemButtons, 0, buttons, 0, ITEMS_PER_PAGE);
        buttons[HARVEST] = harvestButton;
        buttons[LEFT_ARROW] = leftArrow;
        buttons[RIGHT_ARROW] = rightArrow;
        buttons[RETURN] = returnButton;
        this.buttons = new ButtonList(buttons);
    }

    @Override
    public void update(int dt) {
        InputControl input = InputControl.instance();
        if (message != null && input.consumeIfMouseDown(ControlKey.SPACE)) {
            message = null;
            return;
        }

        buttons.update();
        if (buttons.consumeSelectedPress()) {
            updateActiveButtons();
        }

        input.popViewIfEscaped();
    }

    @Override
    public void draw(Graphics g) {
        // Background
        BasicPanels.drawCanvasPanel(g);

        // Info Boxes
        layout.bagPanel.drawBackground(g);

        // Selected item Display
        layout.drawSelectedItem(g, selectedItem);

        // Draw each berry item
        layout.drawItems(g, itemButtons, this.getDisplayBerries(), pageNum);

        // Draw page numbers
        layout.drawPageNumbers(g, pageNum, totalPages());

        // Left and Right arrows
        leftArrow.drawArrow(g, Direction.LEFT);
        rightArrow.drawArrow(g, Direction.RIGHT);

        // Berry Panel
        drawBerryPanel(g);

        // Welcome to the Hellmouth
        harvestButton.fillBorderLabel(g, 20, "Harvest!");
        layout.drawReturnButton(g, returnButton);

        // Tab
        tabPanel.drawBackground(g);
        tabPanel.label(g, 16, "Berries!!");

        // Messages or buttons
        if (!StringUtils.isNullOrWhiteSpace(message)) {
            BasicPanels.drawFullMessagePanel(g, message);
        } else {
            buttons.draw(g);
        }
    }

    private void drawBerryPanel(Graphics g) {
        layout.leftPanel.drawBackground(g);

        TileSet itemTiles = Game.getData().getItemTiles();
        for (int i = 0; i < berryPanels.length; i++) {
            DrawPanel panel = berryPanels[i];
            panel.drawBackground(g);

            PlantedBerry berry = berryFarm.getBerry(i);
            if (berry != null) {
                ImageUtils.drawCenteredImage(
                        g,
                        itemTiles.getTile(berry.getBerry().getImageName()),
                        panel.centerX(),
                        panel.y + panel.height/3
                );

                TextUtils.drawCenteredString(
                        g,
                        berry.getTimeLeftString(),
                        panel.centerX(),
                        panel.y + 3*panel.height/4
                );
            }
        }
    }

    private Set<ItemNamesies> getDisplayBerries() {
        return Game.getPlayer().getBag().getCategory(BagCategory.BERRY);
    }

    private int totalPages() {
        return GeneralUtils.getTotalPages(this.getDisplayBerries().size(), ITEMS_PER_PAGE);
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.BERRY_VIEW;
    }

    @Override
    public void movedToFront() {
        this.berryFarm = Game.getPlayer().getBerryFarm();
        this.message = null;
        this.selectedItem = ItemNamesies.NO_ITEM;

        updateActiveButtons();
    }

    private void updateActiveButtons() {
        Bag bag = Game.getPlayer().getBag();
        if (selectedItem != ItemNamesies.NO_ITEM && !bag.hasItem(selectedItem)) {
            selectedItem = ItemNamesies.NO_ITEM;
        }

        Set<ItemNamesies> berries = this.getDisplayBerries();
        if (selectedItem == ItemNamesies.NO_ITEM) {
            selectedItem = berries.isEmpty() ? ItemNamesies.NO_ITEM : berries.iterator().next();
        }

        int displayed = berries.size();
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            itemButtons[i].setActive(i < displayed - pageNum*ITEMS_PER_PAGE);
        }
    }
}
