package gui.view.item;

import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.layout.DrawLayout;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.PanelList;
import gui.TileSet;
import gui.view.View;
import gui.view.ViewMode;
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
    private static final int ITEMS = 0;
    private static final int BOTTOM_ITEM = ITEMS + ITEMS_PER_PAGE - 1;
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

    public BerryView() {
        this.layout = new BagLayout(true);

        layout.bagPanel.withBackgroundColor(BACKGROUND_COLOR);

        DrawPanel tabPanel = layout.getTabPanel(BagCategory.BERRY.ordinal(), BACKGROUND_COLOR, "Berries!!");

        Button returnButton = layout.createReturnButton(
                new ButtonTransitions().up(RIGHT_ARROW).down(RIGHT_ARROW)
        );

        berryPanels = new DrawLayout(layout.leftPanel, NUM_ROWS, NUM_COLS, 10)
                .withDrawSetup(panel -> panel.withBlackOutline().withFullTransparency())
                .getPanels();

        selectedItem = ItemNamesies.NO_ITEM;
        itemButtons = layout.getItemButtons(
                ITEMS,
                new ButtonTransitions().up(HARVEST).down(RIGHT_ARROW),
                index -> selectedItem = GeneralUtils.getPageValue(this.getDisplayBerries(), pageNum, ITEMS_PER_PAGE, index)
        );

        // Welcome to the Hellmouth
        Button harvestButton = layout.getSelectedButtonLayout(1)
                                     .withStartIndex(HARVEST)
                                     .withDefaultTransitions(new ButtonTransitions().up(RETURN).down(ITEMS))
                                     .withPressIndex(index -> message = berryFarm.harvest(selectedItem))
                                     .withButtonSetup(panel -> panel.withBlackOutline()
                                                                    .withLabel("Harvest!", 20))
                                     .getTabs()[0];

        Button leftArrow = new Button(
                layout.leftArrow,
                new ButtonTransitions().right(RIGHT_ARROW).up(BOTTOM_ITEM - 1).left(RIGHT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        ).asArrow(Direction.LEFT);

        Button rightArrow = new Button(
                layout.rightArrow,
                new ButtonTransitions().right(LEFT_ARROW).up(BOTTOM_ITEM).left(LEFT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        ).asArrow(Direction.RIGHT);

        this.buttons = new ButtonList(NUM_BUTTONS);
        buttons.set(ITEMS, itemButtons);
        buttons.set(HARVEST, harvestButton);
        buttons.set(LEFT_ARROW, leftArrow);
        buttons.set(RIGHT_ARROW, rightArrow);
        buttons.set(RETURN, returnButton);

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
        // Background
        BasicPanels.drawCanvasPanel(g);
        panels.drawAll(g);

        // Draw buttons (without hover)
        buttons.drawPanels(g);

        // Draw page numbers
        layout.drawPageNumbers(g, pageNum, totalPages());

        // Berry Panel
        drawBerryPanel(g);

        // Draw selected item
        layout.drawSelectedItem(g, selectedItem);

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

        layout.setupItems(itemButtons, berries, pageNum);
    }
}
