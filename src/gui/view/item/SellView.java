package gui.view.item;

import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;
import draw.panel.PanelList;
import gui.view.View;
import gui.view.ViewMode;
import input.InputControl;
import item.ItemNamesies;
import item.bag.BagCategory;
import main.Game;
import map.Direction;
import trainer.player.Player;
import util.GeneralUtils;

import java.awt.Graphics;
import java.util.Set;

public class SellView extends View {
    private static final BagCategory[] CATEGORIES = BagCategory.values();
    private static final int ITEMS_PER_PAGE = 10;

    private static final int NUM_BUTTONS = CATEGORIES.length + ITEMS_PER_PAGE + 6;
    private static final int TABS = 0;
    private static final int ITEMS = TABS + CATEGORIES.length;
    private static final int BOTTOM_ITEM = ITEMS + ITEMS_PER_PAGE - 1;
    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int SELL = NUM_BUTTONS - 2;
    private static final int AMOUNT_LEFT_ARROW = NUM_BUTTONS - 3;
    private static final int AMOUNT_RIGHT_ARROW = NUM_BUTTONS - 4;
    private static final int PAGE_RIGHT_ARROW = NUM_BUTTONS - 5;
    private static final int PAGE_LEFT_ARROW = NUM_BUTTONS - 6;

    private final MartLayout layout;
    private final PanelList panels;

    private final ButtonList buttons;
    private final Button[] tabButtons;
    private final Button[] itemButtons;
    private final Button amountLeftButton;
    private final Button amountRightButton;
    private final Button sellButton;

    private int pageNum;
    private int itemAmount;

    private BagCategory selectedTab;
    private ItemNamesies selectedItem;

    public SellView() {
        selectedTab = CATEGORIES[0];
        selectedItem = ItemNamesies.NO_ITEM;

        // Show quantities
        layout = new MartLayout(true);

        Button[] buttons = new Button[NUM_BUTTONS];
        this.buttons = new ButtonList(buttons);

        sellButton = layout.createConfirmButton(
                "SELL",
                new ButtonTransitions().right(RETURN).up(TABS).left(RETURN).down(TABS),
                this::sell
        );

        amountLeftButton = layout.createAmountArrowButton(
                Direction.LEFT,
                new ButtonTransitions().right(AMOUNT_RIGHT_ARROW).up(TABS).left(SELL).down(ITEMS),
                () -> this.updateItemAmount(-1)
        );

        amountRightButton = layout.createAmountArrowButton(
                Direction.RIGHT,
                new ButtonTransitions().right(SELL).up(TABS).left(AMOUNT_LEFT_ARROW).down(ITEMS + 1),
                () -> this.updateItemAmount(1)
        );

        Button returnButton = layout.createReturnButton(
                new ButtonTransitions().right(SELL).up(PAGE_RIGHT_ARROW).left(SELL).down(TABS)
        );

        tabButtons = layout.getTabButtons(TABS, RETURN, AMOUNT_LEFT_ARROW, this::changeCategory);

        itemButtons = layout.getItemButtons(
                ITEMS,
                new ButtonTransitions().up(AMOUNT_RIGHT_ARROW).down(PAGE_RIGHT_ARROW).left(SELL),
                index -> setSelectedItem(GeneralUtils.getPageValue(this.getDisplayItems(), pageNum, ITEMS_PER_PAGE, index))
        );

        Button pageLeftButton = new Button(
                layout.leftArrow,
                new ButtonTransitions().right(PAGE_RIGHT_ARROW).up(BOTTOM_ITEM - 1).left(SELL).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        ).asArrow(Direction.LEFT);

        Button pageRightButton = new Button(
                layout.rightArrow,
                new ButtonTransitions().right(SELL).up(BOTTOM_ITEM).left(PAGE_LEFT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        ).asArrow(Direction.RIGHT);

        System.arraycopy(tabButtons, 0, buttons, TABS, CATEGORIES.length);
        System.arraycopy(itemButtons, 0, buttons, ITEMS, ITEMS_PER_PAGE);
        buttons[PAGE_LEFT_ARROW] = pageLeftButton;
        buttons[PAGE_RIGHT_ARROW] = pageRightButton;
        buttons[SELL] = sellButton;
        buttons[AMOUNT_LEFT_ARROW] = amountLeftButton;
        buttons[AMOUNT_RIGHT_ARROW] = amountRightButton;
        buttons[RETURN] = returnButton;

        this.panels = new PanelList(
                layout.bagPanel, layout.amountPanel, layout.leftPanel,
                layout.playerMoneyPanel, layout.inBagPanel, layout.totalAmountPanel,
                layout.itemsPanel, layout.selectedPanel
        );

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

    private void drawSetup() {
        // Tab colors and outlines and stuff
        layout.setupTabs(tabButtons, selectedTab);

        // Item and amount setup
        layout.setupItems(itemButtons, this.getDisplayItems(), pageNum);
        layout.setup(selectedItem, itemAmount, selectedItem.getItem().getSellPrice()*itemAmount);
    }

    @Override
    public void draw(Graphics g) {
        drawSetup();

        // Background
        BasicPanels.drawCanvasPanel(g);
        panels.drawAll(g);
        buttons.drawPanels(g);

        // Draw page numbers
        layout.drawPageNumbers(g, pageNum, totalPages());

        // Draw selected item and each item in category
        layout.drawItems(g, selectedItem, itemButtons, this.getDisplayItems(), pageNum);

        // Draw button hover
        buttons.drawHover(g);
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

    private Set<ItemNamesies> getDisplayItems() {
        return Game.getPlayer().getBag().getCategory(selectedTab);
    }

    private int totalPages() {
        return GeneralUtils.getTotalPages(this.getDisplayItems().size(), ITEMS_PER_PAGE);
    }

    private void updateActiveButtons() {
        int displayed = this.getDisplayItems().size();
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

        Set<ItemNamesies> list = this.getDisplayItems();
        this.setSelectedItem(list.isEmpty() ? ItemNamesies.NO_ITEM : list.iterator().next());

        // No more items on the current page
        if (list.size() < (pageNum + 1)*ITEMS_PER_PAGE) {
            pageNum = 0;
        }

        updateActiveButtons();
    }

    private void sell() {
        Player player = Game.getPlayer();
        player.getDatCashMoney(itemAmount*selectedItem.getItem().getSellPrice());
        player.getBag().removeItem(selectedItem, itemAmount);

        this.updateCategory();
    }
}
