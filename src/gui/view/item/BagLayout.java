package gui.view.item;

import draw.Alignment;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonPanel;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.panel.DrawLayout.ButtonIndexAction;
import draw.panel.DrawPanel;
import draw.panel.ItemPanel;
import draw.panel.WrapPanel.WrapMetrics;
import gui.TileSet;
import item.Item;
import item.ItemNamesies;
import item.bag.Bag;
import item.bag.BagCategory;
import main.Game;
import main.Global;
import map.Direction;
import util.FontMetrics;
import util.GeneralUtils;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class BagLayout {
    private static final BagCategory[] CATEGORIES = BagCategory.values();
    private static final int NUM_ITEM_ROWS = 5;
    private static final int NUM_ITEM_COLS = 2;
    private static final int ITEMS_PER_PAGE = NUM_ITEM_ROWS*NUM_ITEM_COLS;

    public final DrawPanel bagPanel;
    public final DrawPanel leftPanel;
    public final DrawPanel itemsPanel;
    public final ItemPanel selectedPanel;

    public final DrawPanel[] tabPanels;
    public final DrawPanel[] selectedButtonPanels;
    private final DrawPanel returnPanel;

    public final DrawPanel leftArrow;
    public final DrawPanel rightArrow;

    private final boolean includeQuantity;

    public BagLayout(boolean includeQuantity) {
        this.includeQuantity = includeQuantity;

        int tabHeight = 55;
        int spacing = 28;

        bagPanel = new DrawPanel(
                spacing,
                spacing + tabHeight,
                Point.subtract(
                        Global.GAME_SIZE,
                        2*spacing,
                        2*spacing + tabHeight
                )
        )
                .withTransparentBackground()
                .withBorderPercentage(0)
                .withMissingBlackOutline(Direction.UP);

        int buttonHeight = 38;
        int selectedHeight = 82;
        int halfPanelWidth = (bagPanel.width - 3*spacing)/2;

        leftPanel = new DrawPanel(
                bagPanel.x + spacing,
                bagPanel.y + spacing,
                halfPanelWidth,
                bagPanel.height - 2*spacing
        )
                .withFullTransparency()
                .withBlackOutline();

        selectedPanel = new ItemPanel(
                leftPanel.rightX() + spacing,
                bagPanel.y + spacing,
                halfPanelWidth,
                selectedHeight,
                includeQuantity
        );

        itemsPanel = new DrawPanel(
                selectedPanel.x,
                selectedPanel.bottomY() + buttonHeight + spacing,
                halfPanelWidth,
                leftPanel.height - selectedPanel.height - 2*buttonHeight - 2*spacing
        )
                .withFullTransparency()
                .withBlackOutline();

        tabPanels = new DrawPanel[CATEGORIES.length];
        for (int i = 0; i < tabPanels.length; i++) {
            tabPanels[i] = bagPanel.createTab(i, tabHeight, tabPanels.length);
        }

        selectedButtonPanels = new DrawPanel[UseState.values().length];
        for (int i = 0; i < selectedButtonPanels.length; i++) {
            selectedButtonPanels[i] = selectedPanel.createBottomTab(i, buttonHeight, selectedButtonPanels.length);
        }

        // Fake buttons are fake (just used for spacing)
        Button[] itemButtons = this.getItemButtons(0, new ButtonTransitions(), index -> {});

        int arrowHeight = 20;
        leftArrow = new DrawPanel(
                itemsPanel.x + itemsPanel.width/4,
                itemButtons[itemButtons.length - 1].centerY() + (itemButtons[2].y - itemButtons[0].y) - arrowHeight/2,
                35,
                arrowHeight
        );

        rightArrow = new DrawPanel(
                itemsPanel.rightX() - (leftArrow.x - itemsPanel.x) - leftArrow.width,
                leftArrow.y,
                leftArrow.width,
                leftArrow.height
        );

        returnPanel = new DrawPanel(
                selectedPanel.x,
                bagPanel.bottomY() - spacing - buttonHeight,
                halfPanelWidth,
                buttonHeight
        );
    }

    public Button[] getItemButtons(int startIndex,
                                   ButtonTransitions defaultTransitions,
                                   DrawPanel.ButtonIndexAction indexAction) {
        return itemsPanel.getButtons(
                5,
                NUM_ITEM_ROWS + 1,
                NUM_ITEM_COLS,
                NUM_ITEM_ROWS,
                NUM_ITEM_COLS,
                startIndex,
                defaultTransitions,
                indexAction,
                (index, panel) -> panel.withBackgroundColor(Color.WHITE)
                                       .withBorderPercentage(0)
                                       .withBlackOutline()
        );
    }

    public Button[] getTabButtons(int startIndex, int upIndex, int downIndex, ButtonIndexAction indexAction) {
        Button[] tabButtons = new Button[CATEGORIES.length];
        for (int i = 0; i < tabButtons.length; i++) {
            final int index = i;
            final BagCategory category = CATEGORIES[index];
            tabButtons[i] = new Button(
                    this.tabPanels[i],
                    new ButtonTransitions().up(upIndex)
                                           .down(downIndex)
                                           .basic(Direction.RIGHT, startIndex + i, 1, tabButtons.length)
                                           .basic(Direction.LEFT, startIndex + i, 1, tabButtons.length),
                    () -> indexAction.pressButton(index),
                    panel -> panel.withBorderlessTransparentBackground()
                                  .withBackgroundColor(category.getColor())
                                  .withLabelSize(14, Alignment.LEFT)
                                  .withImageLabel(category.getIcon(), category.getDisplayName())
            );
        }
        return tabButtons;
    }

    public void setupTabs(Button[] tabButtons, BagCategory selectedTab) {
        // Use tab color for background
        bagPanel.withBackgroundColor(selectedTab.getColor());

        // Give correct outline for each tab
        for (int i = 0; i < CATEGORIES.length; i++) {
            tabButtons[i].panel().withTabOutlines(i, selectedTab.ordinal());
        }
    }

    public WrapMetrics drawSelectedItem(Graphics g, ItemNamesies selectedItem) {
        return this.selectedPanel.drawMessage(g, selectedItem);
    }

    public void setupItems(Button[] itemButtons, Iterable<ItemNamesies> items, int pageNum) {
        List<ItemNamesies> pageItems = GeneralUtils.pageValues(items, pageNum, ITEMS_PER_PAGE);
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            itemButtons[i].panel().skipDraw(i >= pageItems.size());
        }
    }

    public void drawItems(Graphics g, ItemNamesies selectedItem, Button[] itemButtons, Iterable<ItemNamesies> items, int pageNum) {
        this.drawSelectedItem(g, selectedItem);

        TileSet itemTiles = Game.getData().getItemTiles();
        Bag bag = Game.getPlayer().getBag();

        FontMetrics.setBlackFont(g, 12);

        List<ItemNamesies> pageItems = GeneralUtils.pageValues(items, pageNum, ITEMS_PER_PAGE);
        for (int i = 0; i < pageItems.size(); i++) {
            ItemNamesies item = pageItems.get(i);
            Item itemValue = item.getItem();
            ButtonPanel panel = itemButtons[i].panel();

            int spacing = panel.getSpace(g);
            int startX = panel.x + spacing + Item.MAX_IMAGE_SIZE.width;
            int centerY = panel.centerY();

            BufferedImage itemImage = itemTiles.getTile(itemValue.getImageName());
            ImageUtils.drawCenteredImage(g, itemImage, (panel.x + startX)/2, centerY);
            TextUtils.drawCenteredHeightString(g, item.getName(), startX, centerY);

            if (includeQuantity && itemValue.hasQuantity()) {
                int rightX = panel.rightX() - spacing;
                TextUtils.drawCenteredHeightString(g, "x" + bag.getQuantity(item), rightX, centerY, Alignment.RIGHT);
            }
        }
    }

    public void drawPageNumbers(Graphics g, int pageNum, int totalPages) {
        TextUtils.drawPageNumbers(g, 16, leftArrow, rightArrow, pageNum, totalPages);
    }

    public Button createReturnButton(ButtonTransitions transitions) {
        return createReturnButton(transitions, ButtonPressAction.getExitAction());
    }

    public Button createReturnButton(ButtonTransitions transitions, ButtonPressAction pressAction) {
        return new Button(
                this.returnPanel,
                transitions,
                pressAction,
                panel -> panel.withTransparentBackground()
                              .withBlackOutline()
                              .withLabel("Return", 20)
        );
    }
}
