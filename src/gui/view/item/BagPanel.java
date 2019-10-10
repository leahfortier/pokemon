package gui.view.item;

import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonTransitions;
import draw.panel.DrawPanel;
import draw.panel.DrawPanel.ButtonIndexAction;
import gui.TileSet;
import item.Item;
import item.ItemNamesies;
import item.bag.Bag;
import item.bag.BagCategory;
import main.Game;
import main.Global;
import map.Direction;
import trainer.Trainer;
import util.FontMetrics;
import util.GeneralUtils;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.Iterator;

class BagPanel {
    private static final BagCategory[] CATEGORIES = BagCategory.values();
    private static final int ITEMS_PER_PAGE = 10;

    public final DrawPanel bagPanel;
    public final DrawPanel leftPanel;
    private final DrawPanel itemsPanel;
    protected final DrawPanel selectedPanel;

    public final DrawPanel[] tabPanels;
    public final DrawPanel[] buttonPanels;
    public final DrawPanel returnPanel;

    public final DrawPanel leftArrow;
    public final DrawPanel rightArrow;

    public BagPanel() {
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
                .withBlackOutline(EnumSet.complementOf(EnumSet.of(Direction.UP)));

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

        selectedPanel = new DrawPanel(
                leftPanel.rightX() + spacing,
                bagPanel.y + spacing,
                halfPanelWidth,
                selectedHeight
        )
                .withFullTransparency()
                .withBlackOutline();

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

        buttonPanels = new DrawPanel[UseState.values().length];
        for (int i = 0; i < buttonPanels.length; i++) {
            buttonPanels[i] = selectedPanel.createBottomTab(i, buttonHeight, buttonPanels.length);
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

    public Button[] getItemButtons(int startIndex, ButtonTransitions defaultTransitions, ButtonIndexAction indexAction) {
        return itemsPanel.getButtons(
                5,
                ITEMS_PER_PAGE/2 + 1,
                2,
                ITEMS_PER_PAGE/2,
                2,
                startIndex,
                defaultTransitions,
                indexAction
        );
    }

    public Button[] getTabButtons(int startIndex, int upIndex, int downIndex, ButtonIndexAction indexAction) {
        Button[] tabButtons = new Button[CATEGORIES.length];
        for (int i = 0; i < tabButtons.length; i++) {
            final int index = i;
            tabButtons[i] = new Button(
                    this.tabPanels[i],
                    new ButtonTransitions().up(upIndex)
                                           .down(downIndex)
                                           .basic(Direction.RIGHT, startIndex + i, 1, tabButtons.length)
                                           .basic(Direction.LEFT, startIndex + i, 1, tabButtons.length),
                    () -> indexAction.pressButton(index)
            );
        }
        return tabButtons;
    }

    public Button[] getLeftButtons(int startIndex, ButtonTransitions defaultTransitions, ButtonIndexAction indexAction) {
        return leftPanel.getButtons(10, Trainer.MAX_POKEMON, 1, startIndex, defaultTransitions, indexAction);
    }

    public void drawSelectedItem(Graphics g, ItemNamesies selectedItem, boolean includeQuantity) {
        selectedPanel.drawBackground(g);

        // Only draw actual items
        if (selectedItem == ItemNamesies.NO_ITEM) {
            return;
        }

        int spacing = 8;

        TileSet itemTiles = Game.getData().getItemTiles();
        Item selectedItemValue = selectedItem.getItem();

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 20);

        // Tile size for image
        int nameX = selectedPanel.x + 2*spacing + Global.TILE_SIZE;
        int startY = selectedPanel.y + FontMetrics.getDistanceBetweenRows(g);

        // Draw item image
        BufferedImage img = itemTiles.getTile(selectedItemValue.getImageName());
        ImageUtils.drawBottomCenteredImage(g, img, selectedPanel.x + (nameX - selectedPanel.x)/2, startY);

        g.drawString(selectedItem.getName(), nameX, startY);

        if (includeQuantity && selectedItemValue.hasQuantity()) {
            String quantityString = "x" + Game.getPlayer().getBag().getQuantity(selectedItem);
            TextUtils.drawRightAlignedString(g, quantityString, selectedPanel.rightX() - 2*spacing, startY);
        }

        FontMetrics.setFont(g, 14);
        TextUtils.drawWrappedText(
                g,
                selectedItemValue.getDescription(),
                selectedPanel.x + spacing,
                startY + FontMetrics.getDistanceBetweenRows(g),
                selectedPanel.width - 2*spacing
        );
    }

    public void drawItems(Graphics g, Button[] itemButtons, Iterable<ItemNamesies> items, int pageNum, boolean includeQuantity) {
        itemsPanel.drawBackground(g);

        TileSet itemTiles = Game.getData().getItemTiles();
        Bag bag = Game.getPlayer().getBag();

        FontMetrics.setFont(g, 12);
        g.setColor(Color.BLACK);

        Iterator<ItemNamesies> iter = GeneralUtils.pageIterator(items, pageNum, ITEMS_PER_PAGE);
        for (int x = 0, k = 0; x < ITEMS_PER_PAGE/2; x++) {
            for (int y = 0; y < 2 && iter.hasNext(); y++, k++) {
                ItemNamesies item = iter.next();
                Item itemValue = item.getItem();
                Button itemButton = itemButtons[k];

                itemButton.fill(g, Color.WHITE);
                itemButton.blackOutline(g);

                g.translate(itemButton.x, itemButton.y);

                ImageUtils.drawCenteredImage(g, itemTiles.getTile(itemValue.getImageName()), 14, 14);
                g.drawString(item.getName(), 29, 18);

                if (includeQuantity && itemValue.hasQuantity()) {
                    TextUtils.drawRightAlignedString(g, "x" + bag.getQuantity(item), 142, 18);
                }

                g.translate(-itemButton.x, -itemButton.y);
            }
        }
    }

    public void drawPageNumbers(Graphics g, int pageNum, int totalPages) {
        FontMetrics.setFont(g, 16);
        TextUtils.drawCenteredString(g, (pageNum + 1) + "/" + totalPages, itemsPanel.centerX(), rightArrow.centerY());
    }

    public void drawTabs(Graphics g, Button[] tabButtons, BagCategory selectedTab) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            Button tabButton = tabButtons[i];
            tabButton.fillTransparent(g, CATEGORIES[i].getColor());
            tabButton.outlineTab(g, i, selectedTab.ordinal());

            g.translate(tabButton.x, tabButton.y);

            g.setColor(Color.BLACK);
            FontMetrics.setFont(g, 14);

            ImageUtils.drawCenteredImage(g, CATEGORIES[i].getIcon(), 16, 26);
            g.drawString(CATEGORIES[i].getDisplayName(), 30, 30);

            g.translate(-tabButton.x, -tabButton.y);
        }
    }

    public void drawReturnButton(Graphics g, Button returnButton) {
        returnButton.fillTransparent(g);
        returnButton.blackOutline(g);
        returnButton.label(g, 20, "Return");
    }
}
