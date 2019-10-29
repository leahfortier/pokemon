package gui.view;

import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;
import draw.panel.DrawLayout;
import draw.panel.DrawPanel;
import draw.panel.PanelList;
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

    private final PanelList panels;
    private final DrawPanel[] berryPanels;

    private final ButtonList buttons;
    private final Button[] itemButtons;

    private int pageNum;
    private String message;
    private ItemNamesies selectedItem;

    private BerryFarm berryFarm;

    BerryView() {
        this.layout = new BagLayout(true);

        layout.bagPanel.withBackgroundColor(BACKGROUND_COLOR)
                       .withBlackOutline();

        DrawPanel tabPanel = layout.tabPanels[BagCategory.BERRY.ordinal()]
                .withBackgroundColor(BACKGROUND_COLOR)
                .withTransparentBackground()
                .withBorderPercentage(0)
                .withMissingBlackOutline(Direction.DOWN)
                .withLabel("Berries!!", 16);

        Button returnButton = layout.createReturnButton(
                new ButtonTransitions().up(RIGHT_ARROW).down(RIGHT_ARROW)
        );

        berryPanels = new DrawLayout(layout.leftPanel, NUM_ROWS, NUM_COLS, 10)
                .withDrawSetup(panel -> panel.withBlackOutline().withFullTransparency())
                .getPanels();

        selectedItem = ItemNamesies.NO_ITEM;
        itemButtons = layout.getItemButtons(
                0,
                new ButtonTransitions().up(HARVEST).down(RIGHT_ARROW),
                index -> selectedItem = GeneralUtils.getPageValue(this.getDisplayBerries(), pageNum, ITEMS_PER_PAGE, index)
        );

        // Harvest button is all the selected buttons at once
        Button harvestButton = new Button(
                layout.selectedPanel.x,
                layout.selectedButtonPanels[0].y,
                layout.selectedPanel.width,
                layout.selectedButtonPanels[0].height,
                new ButtonTransitions().up(RETURN).down(0),
                () -> message = berryFarm.harvest(selectedItem),
                panel -> panel.withTransparentBackground()
                              .withBlackOutline()
                              .withLabel("Harvest!", 20) // Welcome to the Hellmouth
        );

        Button leftArrow = new Button(
                layout.leftArrow,
                new ButtonTransitions().right(RIGHT_ARROW).up(ITEMS_PER_PAGE - 2).left(RIGHT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        ).asArrow(Direction.LEFT);

        Button rightArrow = new Button(
                layout.rightArrow,
                new ButtonTransitions().right(LEFT_ARROW).up(ITEMS_PER_PAGE - 1).left(LEFT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        ).asArrow(Direction.RIGHT);

        Button[] buttons = new Button[NUM_BUTTONS];
        System.arraycopy(itemButtons, 0, buttons, 0, ITEMS_PER_PAGE);
        buttons[HARVEST] = harvestButton;
        buttons[LEFT_ARROW] = leftArrow;
        buttons[RIGHT_ARROW] = rightArrow;
        buttons[RETURN] = returnButton;
        this.buttons = new ButtonList(buttons);

        panels = new PanelList(
                layout.bagPanel, layout.selectedPanel, layout.itemsPanel,
                layout.leftPanel, tabPanel
        ).add(berryPanels);
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
        layout.setupItems(itemButtons, this.getDisplayBerries(), pageNum);

        // Background
        BasicPanels.drawCanvasPanel(g);
        panels.drawAll(g);

        // Draw buttons (without hover)
        buttons.drawPanels(g);

        // Draw page numbers
        layout.drawPageNumbers(g, pageNum, totalPages());

        // Berry Panel
        drawBerryPanel(g);

        // Draw selected item and item buttons
        layout.drawItems(g, selectedItem, itemButtons, this.getDisplayBerries(), pageNum);

        // Messages or buttons
        if (!StringUtils.isNullOrWhiteSpace(message)) {
            BasicPanels.drawFullMessagePanel(g, message);
        } else {
            buttons.drawHover(g);
        }
    }

    private void drawBerryPanel(Graphics g) {
        TileSet itemTiles = Game.getData().getItemTiles();
        for (int i = 0; i < berryPanels.length; i++) {
            DrawPanel panel = berryPanels[i];
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
