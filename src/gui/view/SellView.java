package gui.view;

import draw.DrawUtils;
import draw.ImageUtils;
import draw.PolygonUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import gui.GameData;
import gui.TileSet;
import input.InputControl;
import item.Item;
import item.ItemNamesies;
import item.bag.BagCategory;
import main.Game;
import main.Global;
import map.Direction;
import trainer.player.Player;
import util.FontMetrics;
import util.GeneralUtils;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

class SellView extends View {
    private static final BagCategory[] CATEGORIES = BagCategory.values();
    private static final int ITEMS_PER_PAGE = 10;

    private static final int NUM_BUTTONS = CATEGORIES.length + ITEMS_PER_PAGE + 6;
    private static final int ITEMS = CATEGORIES.length;
    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int SELL = NUM_BUTTONS - 2;
    private static final int AMOUNT_LEFT_ARROW = NUM_BUTTONS - 3;
    private static final int AMOUNT_RIGHT_ARROW = NUM_BUTTONS - 4;
    private static final int PAGE_RIGHT_ARROW = NUM_BUTTONS - 5;
    private static final int PAGE_LEFT_ARROW = NUM_BUTTONS - 6;

    private final DrawPanel bagPanel;
    private final DrawPanel moneyPanel;
    private final DrawPanel itemsPanel;
    private final DrawPanel selectedPanel;
    private final DrawPanel amountPanel;
    private final DrawPanel playerMoneyPanel;
    private final DrawPanel inBagPanel;
    private final DrawPanel sellAmountPanel;

    private final ButtonList buttons;
    private final Button[] tabButtons;
    private final Button[] itemButtons;
    private final Button amountLeftButton;
    private final Button amountRightButton;
    private final Button sellButton;
    private final Button pageLeftButton;
    private final Button pageRightButton;
    private final Button returnButton;

    private int pageNum;
    private int itemAmount;

    private BagCategory selectedTab;
    private ItemNamesies selectedItem;

    SellView() {
        selectedTab = CATEGORIES[0];
        selectedItem = ItemNamesies.NO_ITEM;

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

        moneyPanel = new DrawPanel(
                bagPanel.x + spacing,
                bagPanel.y + spacing,
                halfPanelWidth,
                bagPanel.height - 2*spacing
        )
                .withFullTransparency()
                .withBlackOutline();

        selectedPanel = new DrawPanel(
                moneyPanel.x + moneyPanel.width + spacing,
                bagPanel.y + spacing,
                halfPanelWidth,
                selectedHeight
        )
                .withFullTransparency()
                .withBlackOutline();

        Button[] fakeButtons = moneyPanel.getButtons(10, 6, 1);
        playerMoneyPanel = new DrawPanel(fakeButtons[0]).withBlackOutline();
        inBagPanel = new DrawPanel(fakeButtons[1]).withBlackOutline();
        sellAmountPanel = new DrawPanel(fakeButtons[4]).withBlackOutline();

        Button[] buttons = new Button[NUM_BUTTONS];
        this.buttons = new ButtonList(buttons);

        Button fakeSellButton = fakeButtons[5];
        sellButton = new Button(
                fakeSellButton.x,
                fakeSellButton.y,
                fakeSellButton.width,
                fakeSellButton.height,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(RETURN).up(0).left(RETURN).down(0),
                () -> {
                    Player player = Game.getPlayer();
                    player.getDatCashMoney(itemAmount*selectedItem.getItem().getSellPrice());
                    player.getBag().removeItem(selectedItem, itemAmount);

                    this.updateCategory();

                    if (this.selectedItem == ItemNamesies.NO_ITEM) {
                        this.buttons.setSelected(0);
                    }
                }
        );

        amountLeftButton = new Button(
                selectedPanel.x,
                selectedPanel.y + selectedPanel.height - DrawUtils.OUTLINE_SIZE,
                selectedPanel.width/3,
                buttonHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(AMOUNT_RIGHT_ARROW).up(0).left(SELL).down(ITEMS),
                () -> this.updateItemAmount(-1)
        );

        amountRightButton = new Button(
                selectedPanel.rightX() - amountLeftButton.width,
                amountLeftButton.y,
                amountLeftButton.width,
                amountLeftButton.height,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(AMOUNT_LEFT_ARROW).up(0).left(AMOUNT_LEFT_ARROW).down(ITEMS + 1),
                () -> this.updateItemAmount(1)
        );

        amountPanel = new DrawPanel(
                amountLeftButton.x + amountLeftButton.width - DrawUtils.OUTLINE_SIZE,
                amountLeftButton.y,
                selectedPanel.width - amountLeftButton.width - amountRightButton.width + 2*DrawUtils.OUTLINE_SIZE,
                amountLeftButton.height
        )
                .withFullTransparency()
                .withBlackOutline();

        returnButton = Button.createExitButton(
                selectedPanel.x,
                bagPanel.y + bagPanel.height - spacing - buttonHeight,
                halfPanelWidth,
                buttonHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(SELL).up(PAGE_LEFT_ARROW).left(SELL).down(0)
        );

        itemsPanel = new DrawPanel(
                selectedPanel.x,
                selectedPanel.y + selectedPanel.height + buttonHeight + spacing,
                halfPanelWidth,
                moneyPanel.height - selectedPanel.height - 2*buttonHeight - 2*spacing
        )
                .withFullTransparency()
                .withBlackOutline();

        tabButtons = new Button[CATEGORIES.length];
        for (int i = 0; i < CATEGORIES.length; i++) {
            final int index = i;
            tabButtons[i] = Button.createTabButton(
                    i,
                    bagPanel.x,
                    bagPanel.y,
                    bagPanel.width,
                    tabHeight,
                    tabButtons.length,
                    new ButtonTransitions()
                            .up(RETURN)
                            .down(PAGE_LEFT_ARROW)
                            .basic(Direction.RIGHT, i, 1, CATEGORIES.length)
                            .basic(Direction.LEFT, i, 1, CATEGORIES.length),
                    () -> changeCategory(index)
            );
        }

        itemButtons = itemsPanel.getButtons(
                5,
                ITEMS_PER_PAGE/2 + 1,
                2,
                ITEMS_PER_PAGE/2,
                2,
                ITEMS,
                new ButtonTransitions().up(AMOUNT_RIGHT_ARROW).down(PAGE_RIGHT_ARROW),
                index -> setSelectedItem(GeneralUtils.getPageValue(Game.getPlayer().getBag().getCategory(selectedTab), pageNum, ITEMS_PER_PAGE, index))
        );

        pageLeftButton = new Button(
                498,
                451,
                35,
                20,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(PAGE_RIGHT_ARROW).up(ITEMS_PER_PAGE - 2).left(SELL).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        );

        pageRightButton = new Button(
                613,
                451,
                35,
                20,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(SELL).up(ITEMS_PER_PAGE - 1).left(PAGE_LEFT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        );

        System.arraycopy(tabButtons, 0, buttons, 0, CATEGORIES.length);
        System.arraycopy(itemButtons, 0, buttons, ITEMS, ITEMS_PER_PAGE);
        buttons[PAGE_LEFT_ARROW] = pageLeftButton;
        buttons[PAGE_RIGHT_ARROW] = pageRightButton;
        buttons[SELL] = sellButton;
        buttons[AMOUNT_LEFT_ARROW] = amountLeftButton;
        buttons[AMOUNT_RIGHT_ARROW] = amountRightButton;
        buttons[RETURN] = returnButton;

        this.movedToFront();
    }

    @Override
    public void update(int dt) {
        buttons.update();
        if (buttons.consumeSelectedPress()) {
            updateActiveButtons();
        }

        InputControl.instance().popViewIfEscaped();
    }

    @Override
    public void draw(Graphics g) {
        GameData data = Game.getData();
        Player player = Game.getPlayer();

        TileSet itemTiles = data.getItemTiles();

        // Background
        BasicPanels.drawCanvasPanel(g);

        // Info Boxes
        bagPanel.withBackgroundColor(selectedTab.getColor())
                .drawBackground(g);

        if (!amountLeftButton.isActive()) {
            amountLeftButton.greyOut(g);
            amountRightButton.greyOut(g);
        }

        // Item Display
        selectedPanel.drawBackground(g);
        if (selectedItem != ItemNamesies.NO_ITEM) {
            int spacing = 8;

            Item selectedItemValue = selectedItem.getItem();

            g.setColor(Color.BLACK);
            FontMetrics.setFont(g, 20);

            int startY = selectedPanel.y + FontMetrics.getDistanceBetweenRows(g);
            int nameX = selectedPanel.x + 2*spacing + Global.TILE_SIZE; // TODO: Why are we using Tile Size in the bag view

            // Draw item image
            BufferedImage img = itemTiles.getTile(selectedItemValue.getImageName());
            ImageUtils.drawBottomCenteredImage(g, img, selectedPanel.x + (nameX - selectedPanel.x)/2, startY);

            g.drawString(selectedItem.getName(), nameX, startY);

            FontMetrics.setFont(g, 14);
            TextUtils.drawWrappedText(
                    g,
                    selectedItemValue.getDescription(),
                    selectedPanel.x + spacing,
                    startY + FontMetrics.getDistanceBetweenRows(g),
                    selectedPanel.width - 2*spacing
            );
        }

        amountPanel.drawBackground(g);
        amountPanel.label(g, 20, itemAmount + "");

        amountLeftButton.fillTransparent(g);
        amountLeftButton.blackOutline(g);
        PolygonUtils.drawCenteredArrow(g, amountLeftButton.centerX(), amountLeftButton.centerY(), 35, 20, Direction.LEFT);

        amountRightButton.fillTransparent(g);
        amountRightButton.blackOutline(g);
        PolygonUtils.drawCenteredArrow(g, amountRightButton.centerX(), amountRightButton.centerY(), 35, 20, Direction.RIGHT);

        FontMetrics.setFont(g, 12);
        g.setColor(Color.BLACK);

        // Draw each items in category
        itemsPanel.drawBackground(g);
        Set<ItemNamesies> list = player.getBag().getCategory(selectedTab);
        Iterator<ItemNamesies> iter = GeneralUtils.pageIterator(list, pageNum, ITEMS_PER_PAGE);
        for (int x = 0, k = 0; x < ITEMS_PER_PAGE/2; x++) {
            for (int y = 0; y < 2 && iter.hasNext(); y++, k++) {
                ItemNamesies item = iter.next();
                BufferedImage img = itemTiles.getTile(item.getItem().getImageName());

                Button itemButton = itemButtons[k];
                itemButton.fill(g, Color.WHITE);
                itemButton.blackOutline(g);

                g.translate(itemButton.x, itemButton.y);

                ImageUtils.drawCenteredImage(g, img, 14, 14);
                g.drawString(item.getName(), 29, 18);

                g.translate(-itemButton.x, -itemButton.y);
            }
        }

        // Draw page numbers
        FontMetrics.setFont(g, 16);
        TextUtils.drawCenteredWidthString(g, (pageNum + 1) + "/" + totalPages(), 573, 466);

        // Left and Right arrows
        pageLeftButton.drawArrow(g, Direction.LEFT);
        pageRightButton.drawArrow(g, Direction.RIGHT);

        moneyPanel.drawBackground(g);

        // Player Money
        playerMoneyPanel.drawBackground(g);
        playerMoneyPanel.label(g, 18, "Money: " + Global.MONEY_SYMBOL + player.getDatCashMoney());

        // In bag display
        inBagPanel.drawBackground(g);
        inBagPanel.label(g, 18, "In Bag: " + player.getBag().getQuantity(selectedItem));

        // Total display
        sellAmountPanel.drawBackground(g);
        sellAmountPanel.label(g, 18, "Total: " + Global.MONEY_SYMBOL + selectedItem.getItem().getSellPrice()*itemAmount);

        // Buy button
        sellButton.fillTransparent(g);
        if (!sellButton.isActive()) {
            sellButton.greyOut(g);
        }

        sellButton.label(g, 24, "SELL");
        sellButton.blackOutline(g);

        // Return button
        returnButton.fillTransparent(g);
        returnButton.blackOutline(g);
        returnButton.label(g, 20, "Return");

        // Tab
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

        buttons.draw(g);
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.SELL_VIEW;
    }

    @Override
    public void movedToFront() {
        // Set selected button to be the first tab and switch to first tab
        this.buttons.setSelected(0);
        this.changeCategory(0);
    }

    private void updateItemAmount(int delta) {
        int numItems = Game.getPlayer().getBag().getQuantity(this.selectedItem);
        this.itemAmount = GeneralUtils.wrapIncrement(this.itemAmount, delta, 1, numItems);
    }

    private int totalPages() {
        int size = Game.getPlayer().getBag().getCategory(selectedTab).size();
        return GeneralUtils.getTotalPages(size, ITEMS_PER_PAGE);
    }

    private void updateActiveButtons() {
        int displayed = Game.getPlayer().getBag().getCategory(selectedTab).size();
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            itemButtons[i].setActive(i < displayed - pageNum*ITEMS_PER_PAGE);
        }

        boolean amountSet = itemAmount > 0;
        amountLeftButton.setActive(amountSet);
        amountRightButton.setActive(amountSet);
        sellButton.setActive(amountSet);
    }

    private void setSelectedItem(ItemNamesies item) {
        selectedItem = item;
        itemAmount = selectedItem.getItem().getPrice() > 0 ? 1 : 0;
    }

    private void updateCategory() {
        changeCategory(this.selectedTab.ordinal());
    }

    private void changeCategory(int index) {
        if (selectedTab.ordinal() != index) {
            pageNum = 0;
        }

        selectedTab = CATEGORIES[index];

        Set<ItemNamesies> list = Game.getPlayer().getBag().getCategory(selectedTab);
        this.setSelectedItem(list.isEmpty() ? ItemNamesies.NO_ITEM : list.iterator().next());

        // No more items on the current page
        if (list.size() < (pageNum + 1)*ITEMS_PER_PAGE) {
            pageNum = 0;
        }

        updateActiveButtons();
    }
}
