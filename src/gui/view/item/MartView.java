package gui.view.item;

import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.PanelList;
import gui.view.View;
import gui.view.ViewMode;
import input.InputControl;
import item.Item;
import item.ItemNamesies;
import main.Game;
import map.Direction;
import trainer.player.Badge;
import trainer.player.Player;
import trainer.player.medal.Medal;
import trainer.player.medal.MedalTheme;
import util.GeneralUtils;
import util.string.PokeString;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MartView extends View {
    private static final Color BACKGROUND_COLOR = new Color(68, 123, 184);
    private static final int ITEMS_PER_PAGE = 10;

    private static final int NUM_BUTTONS = ITEMS_PER_PAGE + 6;
    private static final int ITEMS = 0;
    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int BUY = NUM_BUTTONS - 2;
    private static final int AMOUNT_LEFT_ARROW = NUM_BUTTONS - 3;
    private static final int AMOUNT_RIGHT_ARROW = NUM_BUTTONS - 4;
    private static final int PAGE_RIGHT_ARROW = NUM_BUTTONS - 5;
    private static final int PAGE_LEFT_ARROW = NUM_BUTTONS - 6;

    private final MartLayout layout;
    private final PanelList panels;

    private final ButtonList buttons;
    private final Button[] itemButtons;
    private final Button amountLeftButton;
    private final Button amountRightButton;
    private final Button buyButton;

    private int pageNum;
    private int itemAmount;

    private List<ItemNamesies> forSaleItems;
    private ItemNamesies selectedItem;

    public MartView() {
        // No quantities
        layout = new MartLayout(false);

        layout.bagPanel.withBackgroundColor(BACKGROUND_COLOR)
                       .withBlackOutline();

        DrawPanel tabPanel = layout.tabPanels[1]
                .withBackgroundColor(BACKGROUND_COLOR)
                .withBorderlessTransparentBackground()
                .withMissingBlackOutline(Direction.DOWN)
                .withLabel(PokeString.POKE + " Mart", 16);

        buyButton = layout.createConfirmButton(
                "BUY",
                new ButtonTransitions().right(RETURN).left(RETURN),
                this::buy
        );

        amountLeftButton = layout.createAmountArrowButton(
                Direction.LEFT,
                new ButtonTransitions().right(AMOUNT_RIGHT_ARROW).up(RETURN).left(BUY).down(ITEMS),
                () -> this.updateItemAmount(-1)
        );

        amountRightButton = layout.createAmountArrowButton(
                Direction.RIGHT,
                new ButtonTransitions().right(BUY).up(RETURN).left(AMOUNT_LEFT_ARROW).down(ITEMS + 1),
                () -> this.updateItemAmount(1)
        );

        Button returnButton = layout.createReturnButton(
                new ButtonTransitions().right(BUY).up(PAGE_LEFT_ARROW).left(BUY).down(AMOUNT_LEFT_ARROW)
        );

        itemButtons = layout.getItemButtons(
                ITEMS,
                new ButtonTransitions().up(AMOUNT_LEFT_ARROW).down(PAGE_LEFT_ARROW).left(BUY).right(BUY),
                index -> setSelectedItem(GeneralUtils.getPageValue(forSaleItems, pageNum, ITEMS_PER_PAGE, index))
        );

        Button pageLeftButton = new Button(
                layout.leftArrow,
                new ButtonTransitions().right(PAGE_RIGHT_ARROW).up(ITEMS_PER_PAGE - 2).left(BUY).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        ).asArrow(Direction.LEFT);

        Button pageRightButton = new Button(
                layout.rightArrow,
                new ButtonTransitions().right(BUY).up(ITEMS_PER_PAGE - 1).left(PAGE_LEFT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        ).asArrow(Direction.RIGHT);

        Button[] buttons = new Button[NUM_BUTTONS];
        System.arraycopy(itemButtons, 0, buttons, ITEMS, ITEMS_PER_PAGE);
        buttons[PAGE_LEFT_ARROW] = pageLeftButton;
        buttons[PAGE_RIGHT_ARROW] = pageRightButton;
        buttons[BUY] = buyButton;
        buttons[AMOUNT_LEFT_ARROW] = amountLeftButton;
        buttons[AMOUNT_RIGHT_ARROW] = amountRightButton;
        buttons[RETURN] = returnButton;

        this.buttons = new ButtonList(buttons);
        this.panels = new PanelList(
                layout.bagPanel, layout.amountPanel, layout.leftPanel, layout.playerMoneyPanel,
                layout.inBagPanel, layout.totalAmountPanel, tabPanel, layout.itemsPanel, layout.selectedPanel
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

    @Override
    public void draw(Graphics g) {
        // Item and amount setup
        layout.setupItems(itemButtons, forSaleItems, pageNum);
        layout.setup(selectedItem, itemAmount, selectedItem.getItem().getPrice()*itemAmount);

        // Background
        BasicPanels.drawCanvasPanel(g);
        panels.drawAll(g);
        buttons.drawPanels(g);

        // Draw page numbers
        layout.drawPageNumbers(g, pageNum, totalPages());

        // Draw selected item and for sale items
        layout.drawItems(g, selectedItem, itemButtons, forSaleItems, pageNum);

        // Draw hover action
        buttons.drawHover(g);
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.MART_VIEW;
    }

    private void resetForSaleItems() {
        Player player = Game.getPlayer();

        this.forSaleItems = new ArrayList<>();
        Collections.addAll(
                forSaleItems,
                ItemNamesies.POTION,
                ItemNamesies.POKE_BALL,
                ItemNamesies.ANTIDOTE,
                ItemNamesies.PARALYZE_HEAL,
                ItemNamesies.BURN_HEAL,
                ItemNamesies.AWAKENING,
                ItemNamesies.ICE_HEAL
        );

        if (player.hasBadge(Badge.ROUND)) {
            Collections.addAll(
                    forSaleItems,
                    ItemNamesies.GREAT_BALL,
                    ItemNamesies.SUPER_POTION,
                    ItemNamesies.REPEL
            );
        }

        if (player.hasBadge(Badge.SECOND)) {
            Collections.addAll(
                    forSaleItems,
                    ItemNamesies.REVIVE
            );
        }

        if (player.hasBadge(Badge.THIRD)) {
            Collections.addAll(
                    forSaleItems,
                    ItemNamesies.SUPER_REPEL
            );
        }

        if (player.hasBadge(Badge.FOURTH)) {
            Collections.addAll(
                    forSaleItems,
                    ItemNamesies.ULTRA_BALL,
                    ItemNamesies.HYPER_POTION
            );
        }

        if (player.hasBadge(Badge.FIFTH)) {
            Collections.addAll(
                    forSaleItems,
                    ItemNamesies.FULL_HEAL,
                    ItemNamesies.MAX_REPEL
            );
        }

        if (player.hasBadge(Badge.SIXTH)) {
            Collections.addAll(
                    forSaleItems,
                    ItemNamesies.MAX_POTION
            );
        }

        if (player.hasBadge(Badge.SEVENTH)) {
            Collections.addAll(
                    forSaleItems,
                    ItemNamesies.FULL_RESTORE
            );
        }

        this.forSaleItems.sort((firstItemName, secondItemName) -> {
            Item firstItem = firstItemName.getItem();
            Item secondItem = secondItemName.getItem();

            if (firstItem.getBagCategory() != secondItem.getBagCategory()) {
                return firstItem.getBagCategory().ordinal() - secondItem.getBagCategory().ordinal();
            }

            if (firstItem.getPrice() != secondItem.getPrice()) {
                return firstItem.getPrice() - secondItem.getPrice();
            }

            return firstItem.getName().compareTo(secondItem.getName());
        });
    }

    @Override
    public void movedToFront() {
        this.resetForSaleItems();
        this.setSelectedItem(forSaleItems.get(0));
        this.updateActiveButtons();
    }

    private void updateItemAmount(int delta) {
        this.itemAmount = GeneralUtils.wrapIncrement(this.itemAmount, delta, 1, this.maxPurchaseAmount());
    }

    private int maxPurchaseAmount() {
        return Game.getPlayer().getDatCashMoney()/selectedItem.getItem().getPrice();
    }

    private int totalPages() {
        return GeneralUtils.getTotalPages(forSaleItems.size(), ITEMS_PER_PAGE);
    }

    private void updateActiveButtons() {
        int displayed = forSaleItems.size();
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            itemButtons[i].setActive(i < displayed - pageNum*ITEMS_PER_PAGE);
        }

        boolean amountSet = itemAmount > 0;
        amountLeftButton.setActive(amountSet);
        amountRightButton.setActive(amountSet);
        buyButton.setActive(amountSet);
    }

    private void setSelectedItem(ItemNamesies item) {
        selectedItem = item;
        itemAmount = selectedItem.getItem().getPrice() <= Game.getPlayer().getDatCashMoney() ? 1 : 0;
    }

    private void buy() {
        Player player = Game.getPlayer();
        player.sucksToSuck(itemAmount*selectedItem.getItem().getPrice());
        player.getBag().addItem(selectedItem, itemAmount);
        player.getMedalCase().increase(MedalTheme.ITEMS_BOUGHT, itemAmount);

        if (selectedItem == ItemNamesies.POKE_BALL && itemAmount >= 10) {
            player.getBag().addItem(ItemNamesies.PREMIER_BALL);
            player.getMedalCase().earnMedal(Medal.SMART_SHOPPER);
        }

        setSelectedItem(selectedItem);
    }
}
