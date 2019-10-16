package gui.view.item;

import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;
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
    private static final int ITEMS = CATEGORIES.length;
    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int SELL = NUM_BUTTONS - 2;
    private static final int AMOUNT_LEFT_ARROW = NUM_BUTTONS - 3;
    private static final int AMOUNT_RIGHT_ARROW = NUM_BUTTONS - 4;
    private static final int PAGE_RIGHT_ARROW = NUM_BUTTONS - 5;
    private static final int PAGE_LEFT_ARROW = NUM_BUTTONS - 6;

    private final MartPanel panel;

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

    public SellView() {
        selectedTab = CATEGORIES[0];
        selectedItem = ItemNamesies.NO_ITEM;

        panel = new MartPanel();

        Button[] buttons = new Button[NUM_BUTTONS];
        this.buttons = new ButtonList(buttons);

        sellButton = new Button(
                panel.confirmPanel,
                new ButtonTransitions().right(RETURN).up(TABS).left(RETURN).down(TABS),
                this::sell
        );

        amountLeftButton = new Button(
                panel.buttonPanels[0],
                new ButtonTransitions().right(AMOUNT_RIGHT_ARROW).up(TABS).left(SELL).down(ITEMS),
                () -> this.updateItemAmount(-1)
        );

        amountRightButton = new Button(
                panel.buttonPanels[2],
                new ButtonTransitions().right(SELL).up(TABS).left(AMOUNT_LEFT_ARROW).down(ITEMS + 1),
                () -> this.updateItemAmount(1)
        );

        returnButton = new Button(
                panel.returnPanel,
                new ButtonTransitions().right(SELL).up(PAGE_LEFT_ARROW).left(SELL).down(TABS),
                ButtonPressAction.getExitAction()
        );

        tabButtons = panel.getTabButtons(TABS, RETURN, AMOUNT_LEFT_ARROW, this::changeCategory);

        itemButtons = panel.getItemButtons(
                ITEMS,
                new ButtonTransitions().up(AMOUNT_LEFT_ARROW).down(PAGE_LEFT_ARROW),
                index -> setSelectedItem(GeneralUtils.getPageValue(this.getDisplayItems(), pageNum, ITEMS_PER_PAGE, index))
        );

        pageLeftButton = new Button(
                panel.leftArrow,
                new ButtonTransitions().right(PAGE_RIGHT_ARROW).up(ITEMS_PER_PAGE - 2).left(SELL).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        );

        pageRightButton = new Button(
                panel.rightArrow,
                new ButtonTransitions().right(SELL).up(ITEMS_PER_PAGE - 1).left(PAGE_LEFT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        );

        System.arraycopy(tabButtons, 0, buttons, TABS, CATEGORIES.length);
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
        // Background
        BasicPanels.drawCanvasPanel(g);

        // Info Boxes
        panel.bagPanel.withBackgroundColor(selectedTab.getColor())
                      .drawBackground(g);

        // Item Display
        panel.drawSelectedItem(g, selectedItem);

        // Draw selected amount and arrows
        panel.drawAmount(g, itemAmount, amountLeftButton, amountRightButton);

        // Draw each items in category
        panel.drawItems(g, itemButtons, this.getDisplayItems(), pageNum, true);

        // Draw page numbers
        panel.drawPageNumbers(g, pageNum, totalPages());

        // Left and Right arrows
        pageLeftButton.drawArrow(g, Direction.LEFT);
        pageRightButton.drawArrow(g, Direction.RIGHT);

        // Left panel -- player money, in bag amount, total sell price
        panel.drawMoneyPanel(g, selectedItem, selectedItem.getItem().getSellPrice()*itemAmount);

        // Sell button
        panel.drawConfirmButton(g, sellButton, "SELL");

        // Return button
        panel.drawReturnButton(g, returnButton);

        // Tabs
        panel.drawTabs(g, tabButtons, selectedTab);

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
