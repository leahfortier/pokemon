package gui.view.battle.handler;

import battle.Battle;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.panel.DrawPanel;
import draw.panel.WrapPanel;
import draw.panel.WrapPanel.WrapMetrics;
import gui.TileSet;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import item.ItemNamesies;
import item.bag.Bag;
import item.bag.BattleBagCategory;
import item.use.PokemonUseItem;
import main.Game;
import map.Direction;
import trainer.TrainerAction;
import trainer.player.Player;
import util.FontMetrics;
import util.GeneralUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Set;

public class BagState implements VisualStateHandler {

    // Battle Bag Categories
    private static final BattleBagCategory[] BATTLE_BAG_CATEGORIES = BattleBagCategory.values();

    // Bag Button Indexes
    private static final int ITEMS = BATTLE_BAG_CATEGORIES.length;
    private static final int ITEMS_PER_PAGE = 10;
    private static final int NUM_BAG_BUTTONS = BATTLE_BAG_CATEGORIES.length + ITEMS_PER_PAGE + 3;
    private static final int LAST_ITEM_BUTTON = NUM_BAG_BUTTONS - 1;
    private static final int BAG_RIGHT_BUTTON = NUM_BAG_BUTTONS - 2;
    private static final int BAG_LEFT_BUTTON = NUM_BAG_BUTTONS - 3;

    private final DrawPanel bagCategoryPanel;
    private final WrapPanel lastItemPanel;

    private final ButtonList bagButtons;
    private final Button[] bagTabButtons;
    private final Button[] bagItemButtons;
    private final Button bagRightButton;
    private final Button bagLeftButton;
    private final Button bagLastUsedBtn;

    // Current bag page, bag category, and selected item
    private int bagPage;
    private int selectedBagTab;
    private ItemNamesies selectedItem;

    public BagState() {
        bagCategoryPanel = new DrawPanel(30, 218, 357, 259)
                .withBorderPercentage(6);

        lastItemPanel = new WrapPanel(bagCategoryPanel.x, 492, bagCategoryPanel.width, 78, 13)
                .withBorderPercentage(17)
                .withBlackOutline();

        // Bag View Buttons
        Button[] bagButtons = new Button[NUM_BAG_BUTTONS];

        bagTabButtons = new Button[BATTLE_BAG_CATEGORIES.length];
        for (int i = 0; i < BATTLE_BAG_CATEGORIES.length; i++) {
            bagButtons[i] = bagTabButtons[i] = new Button(
                    bagCategoryPanel.createTab(i, 28, bagTabButtons.length),
                    new ButtonTransitions()
                            .up(LAST_ITEM_BUTTON)
                            .down(ITEMS)
                            .basic(Direction.RIGHT, i, 1, BATTLE_BAG_CATEGORIES.length)
                            .basic(Direction.LEFT, i, 1, BATTLE_BAG_CATEGORIES.length)
            );
        }

        bagButtons[BAG_LEFT_BUTTON] = bagLeftButton = new Button(
                135, 435, 35, 20, ButtonHoverAction.BOX,
                new ButtonTransitions().right(BAG_RIGHT_BUTTON).up(ITEMS + ITEMS_PER_PAGE - 2).down(LAST_ITEM_BUTTON)
        );

        bagButtons[BAG_RIGHT_BUTTON] = bagRightButton = new Button(
                250, 435, 35, 20, ButtonHoverAction.BOX,
                new ButtonTransitions().up(ITEMS + ITEMS_PER_PAGE - 1).left(BAG_LEFT_BUTTON).down(LAST_ITEM_BUTTON)
        );

        bagButtons[LAST_ITEM_BUTTON] = bagLastUsedBtn = new Button(
                214, 517, 148, 28, ButtonHoverAction.BOX,
                new ButtonTransitions().up(BAG_LEFT_BUTTON).down(selectedBagTab)
        );

        bagItemButtons = new Button[ITEMS_PER_PAGE];
        for (int y = 0, i = 0; y < ITEMS_PER_PAGE/2; y++) {
            for (int x = 0; x < 2; x++, i++) {
                bagItemButtons[i] = bagButtons[i + ITEMS] = new Button(
                        55 + x*162,
                        243 + y*38,
                        148,
                        28,
                        ButtonHoverAction.BOX,
                        new ButtonTransitions()
                                .right((i + 1)%ITEMS_PER_PAGE + ITEMS)
                                .up(y == 0 ? selectedBagTab : i + ITEMS - 2)
                                .left((i - 1 + ITEMS_PER_PAGE)%ITEMS_PER_PAGE + ITEMS)
                                .down(y == ITEMS_PER_PAGE/2 - 1 ? (x == 0 ? BAG_LEFT_BUTTON : BAG_RIGHT_BUTTON) : i + ITEMS + 2)
                );
            }
        }

        this.bagButtons = new ButtonList(bagButtons);
    }

    @Override
    public void reset() {
        selectedBagTab = 0;
        bagPage = 0;
    }

    @Override
    public void set(BattleView view) {
        Bag playerBag = Game.getPlayer().getBag();
        int pageSize = this.getDisplayItems().size();

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            bagItemButtons[i].setActive(i < pageSize - bagPage*ITEMS_PER_PAGE);
        }

        // TODO: Make a method for this
        bagLastUsedBtn.setActive(playerBag.getLastUsedItem() != ItemNamesies.NO_ITEM);

        bagButtons.setFalseHover();
    }

    @Override
    public void draw(BattleView view, Graphics g) {
        view.drawLargeMenuPanel(g);

        BattleBagCategory selectedCategory = BATTLE_BAG_CATEGORIES[selectedBagTab];
        bagCategoryPanel.withTransparentBackground(selectedCategory.getColor())
                        .withMissingBlackOutline(Direction.UP)
                        .drawBackground(g);

        lastItemPanel.withTransparentBackground(selectedCategory.getColor())
                     .drawBackground(g);

        // Tabs
        for (int i = 0; i < BATTLE_BAG_CATEGORIES.length; i++) {
            Button tabButton = bagTabButtons[i];
            BattleBagCategory category = BATTLE_BAG_CATEGORIES[i];

            tabButton.fill(g, category.getColor());
            tabButton.label(g, 18, category.getName());
            tabButton.outlineTab(g, i, selectedBagTab);
        }

        // Black outline around the entire box
        DrawUtils.blackOutline(
                g,
                bagCategoryPanel.x,
                bagTabButtons[0].y,
                bagCategoryPanel.width,
                bagCategoryPanel.bottomY() - bagTabButtons[0].y
        );

        // Messages text
        String message = view.getMessage(VisualState.INVALID_BAG, "Choose an item!");
        view.drawMenuMessagePanel(g, message);

        Bag bag = Game.getPlayer().getBag();

        Set<ItemNamesies> items = getDisplayItems();
        TileSet itemTiles = Game.getData().getItemTiles();

        int selectedButton = bagButtons.getSelected();
        ItemNamesies selected = null;

        FontMetrics.setBlackFont(g, 12);
        Iterator<ItemNamesies> iter = GeneralUtils.pageIterator(items, bagPage, ITEMS_PER_PAGE);
        for (int i = 0; i < ITEMS_PER_PAGE && iter.hasNext(); i++) {
            ItemNamesies item = iter.next();
            drawItemButton(g, itemTiles, bagItemButtons[i], item);

            if (selectedButton == ITEMS + i) {
                selected = item;
            }
        }

        // Show last item used if no item is selected
        if (selected == null) {
            // Last Item Used
            ItemNamesies lastUsedItem = bag.getLastUsedItem();
            lastItemPanel.drawLeftLabel(g, 16, "Last item used:");

            // TODO: Should have a method to check if it is the empty item
            if (lastUsedItem != ItemNamesies.NO_ITEM) {
                drawItemButton(g, itemTiles, bagLastUsedBtn, lastUsedItem);
            }
        }
        // Otherwise, draw selected item's information
        else {
            drawItemDescription(g, selected);
        }

        // Bag page numbers and arrows
        TextUtils.drawPageNumbers(g, 20, bagLeftButton, bagRightButton, bagPage, totalPages());
        bagLeftButton.drawArrow(g, Direction.LEFT);
        bagRightButton.drawArrow(g, Direction.RIGHT);

        // Back Arrow
        view.drawBackButton(g);

        bagButtons.drawHover(g);
    }

    private Set<ItemNamesies> getDisplayItems() {
        return Game.getPlayer().getBag().getCategory(BATTLE_BAG_CATEGORIES[selectedBagTab]);
    }

    private int totalPages() {
        return GeneralUtils.getTotalPages(this.getDisplayItems().size(), ITEMS_PER_PAGE);
    }

    private void drawItemButton(Graphics g, TileSet itemTiles, Button button, ItemNamesies itemNamesies) {

        // Draw box
        button.fill(g, Color.WHITE);
        button.blackOutline(g);

        int dx = button.x;
        int dy = button.y;

        g.translate(dx, dy);

        BufferedImage img = itemTiles.getTile(itemNamesies.getItem().getImageName());
        ImageUtils.drawCenteredImage(g, img, 14, 14);

        FontMetrics.setBlackFont(g, 12);

        g.drawString(itemNamesies.getName(), 28, 19);
        TextUtils.drawRightAlignedString(g, "x" + Game.getPlayer().getBag().getQuantity(itemNamesies), 140, 19);

        g.translate(-dx, -dy);
    }

    public WrapMetrics drawItemDescription(Graphics g, ItemNamesies itemNamesies) {
        return lastItemPanel.drawMessage(g, itemNamesies.getItem().getDescription());
    }

    @Override
    public void update(BattleView view) {
        // Update all bag buttons and the back button
        bagButtons.update();

        // Check tabs
        for (int i = 0; i < BATTLE_BAG_CATEGORIES.length; i++) {
            if (bagTabButtons[i].checkConsumePress()) {
                bagPage = 0;
                selectedBagTab = i;
                view.setVisualState(); // To update active buttons
            }
        }

        Battle currentBattle = view.getCurrentBattle();
        Player player = Game.getPlayer();
        Bag bag = player.getBag();

        Set<ItemNamesies> items = this.getDisplayItems();
        Iterator<ItemNamesies> iter = GeneralUtils.pageIterator(items, bagPage, ITEMS_PER_PAGE);

        // Go through each item on the page
        for (int i = 0; i < ITEMS_PER_PAGE && iter.hasNext(); i++) {
            ItemNamesies item = iter.next();
            if (bagItemButtons[i].checkConsumePress()) {
                // Pokemon Use Item -- Set item to be selected an change to Pokemon View
                if (item.getItem() instanceof PokemonUseItem) {
                    selectedItem = item;
                    view.setVisualState(VisualState.USE_ITEM);
                    break;
                }
                // Otherwise, just use it on the battle if successful
                else if (bag.battleUseItem(item, player.front(), currentBattle)) {
                    player.performAction(currentBattle, TrainerAction.ITEM);
                    view.setVisualState(VisualState.MENU);
                    view.cycleMessage();
                    break;
                }
                // If the item cannot be used, do not consume
                else {
                    view.cycleMessage();
                    view.setVisualState(VisualState.INVALID_BAG);
                }
            }
        }

        // Selecting the Last Item Used Button
        if (bagLastUsedBtn.checkConsumePress()) {
            ItemNamesies lastItemUsed = bag.getLastUsedItem();
            if (lastItemUsed != ItemNamesies.NO_ITEM && bag.battleUseItem(lastItemUsed, player.front(), currentBattle)) {
                player.performAction(currentBattle, TrainerAction.ITEM);
                view.setVisualState(VisualState.MENU);
                view.cycleMessage();
            } else {
                view.cycleMessage();
                view.setVisualState(VisualState.INVALID_BAG);
            }
        }

        int increment = 0;
        if (bagRightButton.checkConsumePress()) {
            // Next page
            increment = 1;
        }
        if (bagLeftButton.checkConsumePress()) {
            // Previous Page
            increment = -1;
        }

        if (increment != 0) {
            bagPage = GeneralUtils.wrapIncrement(bagPage, increment, totalPages(items));
            view.setVisualState(); // To update active buttons
        }

        // Return to main battle menu
        view.updateBackButton();
    }

    public ItemNamesies getSelectedItem() {
        return this.selectedItem;
    }

    private int totalPages(Set<ItemNamesies> items) {
        return GeneralUtils.getTotalPages(items.size(), ITEMS_PER_PAGE);
    }

    @Override
    public ButtonList getButtons() {
        return this.bagButtons;
    }
}
