package gui.view.battle.handler;

import battle.Battle;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.panel.DrawPanel;
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
import java.util.EnumSet;
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
    private final DrawPanel lastItemPanel;

    private final Button bagRightButton;
    private final Button bagLeftButton;
    private final Button bagLastUsedBtn;

    private final Button[] bagButtons;
    private final Button[] bagTabButtons;

    // Current bag page, bag category, and selected item
    private int bagPage;
    private int selectedBagTab;
    private ItemNamesies selectedItem;

    public BagState() {
        bagCategoryPanel = new DrawPanel(30, 218, 357, 259)
                .withBorderPercentage(6);

        lastItemPanel = new DrawPanel(bagCategoryPanel.x, 492, bagCategoryPanel.width, 78)
                .withBorderPercentage(17)
                .withBlackOutline();

        // Bag View Buttons
        bagButtons = new Button[NUM_BAG_BUTTONS];

        bagTabButtons = new Button[BATTLE_BAG_CATEGORIES.length];
        for (int i = 0; i < BATTLE_BAG_CATEGORIES.length; i++) {
            bagButtons[i] = bagTabButtons[i] = Button.createTabButton(
                    i,
                    bagCategoryPanel.x,
                    bagCategoryPanel.y,
                    bagCategoryPanel.width,
                    28,
                    bagTabButtons.length,
                    new int[] {
                            Button.basicTransition(i, 1, BATTLE_BAG_CATEGORIES.length, Direction.RIGHT), // Right
                            LAST_ITEM_BUTTON, // Up
                            Button.basicTransition(i, 1, BATTLE_BAG_CATEGORIES.length, Direction.LEFT), // Left
                            ITEMS  // Down
                    }
            );
        }

        bagButtons[BAG_LEFT_BUTTON] = bagLeftButton = new Button(135, 435, 35, 20, ButtonHoverAction.BOX, new int[] {
                BAG_RIGHT_BUTTON,
                ITEMS + ITEMS_PER_PAGE - 2,
                -1,
                LAST_ITEM_BUTTON
        });
        bagButtons[BAG_RIGHT_BUTTON] = bagRightButton = new Button(250, 435, 35, 20, ButtonHoverAction.BOX, new int[] {
                -1,
                ITEMS + ITEMS_PER_PAGE - 1,
                BAG_LEFT_BUTTON,
                LAST_ITEM_BUTTON
        });
        bagButtons[LAST_ITEM_BUTTON] = bagLastUsedBtn = new Button(214, 517, 148, 28, ButtonHoverAction.BOX, new int[] {
                -1,
                BAG_LEFT_BUTTON,
                -1,
                selectedBagTab
        });

        for (int y = 0, i = ITEMS; y < ITEMS_PER_PAGE/2; y++) {
            for (int x = 0; x < 2; x++, i++) {
                bagButtons[i] = new Button(
                        55 + x*162,
                        243 + y*38,
                        148,
                        28,
                        ButtonHoverAction.BOX,
                        new int[] {
                                (i + 1 - ITEMS)%ITEMS_PER_PAGE + ITEMS,
                                y == 0 ? selectedBagTab : i - 2,
                                (i - 1 - ITEMS + ITEMS_PER_PAGE)%ITEMS_PER_PAGE + ITEMS,
                                y == ITEMS_PER_PAGE/2 - 1 ? (x == 0 ? BAG_LEFT_BUTTON : BAG_RIGHT_BUTTON) : i + 2
                        }
                );
            }
        }
    }

    @Override
    public void reset() {
        selectedBagTab = 0;
        bagPage = 0;
    }

    @Override
    public void set(BattleView view) {
        Bag playerBag = Game.getPlayer().getBag();
        int pageSize = playerBag.getCategory(BATTLE_BAG_CATEGORIES[selectedBagTab]).size();

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            bagButtons[ITEMS + i].setActive(i < pageSize - bagPage*ITEMS_PER_PAGE);
        }

        // TODO: Make a method for this
        bagLastUsedBtn.setActive(playerBag.getLastUsedItem() != ItemNamesies.NO_ITEM);

        for (Button button : bagButtons) {
            button.setForceHover(false);
        }
    }

    @Override
    public void draw(BattleView view, Graphics g) {
        view.drawLargeMenuPanel(g);

        BattleBagCategory selectedCategory = BATTLE_BAG_CATEGORIES[selectedBagTab];
        bagCategoryPanel.withTransparentBackground(selectedCategory.getColor())
                        .withBlackOutline(EnumSet.complementOf(EnumSet.of(Direction.UP)))
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

        Set<ItemNamesies> toDraw = bag.getCategory(BATTLE_BAG_CATEGORIES[selectedBagTab]);
        TileSet itemTiles = Game.getData().getItemTiles();

        int selectedButton = view.getSelectedButton();
        ItemNamesies selected = null;

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 12);
        Iterator<ItemNamesies> iter = GeneralUtils.pageIterator(toDraw, bagPage, ITEMS_PER_PAGE);
        for (int i = 0; i < ITEMS_PER_PAGE && iter.hasNext(); i++) {
            ItemNamesies item = iter.next();
            drawItemButton(g, itemTiles, bagButtons[ITEMS + i], item);

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
            lastItemPanel.drawMessage(g, 12, selected.getItem().getDescription());
        }

        // Bag page number
        FontMetrics.setFont(g, 20);
        TextUtils.drawCenteredWidthString(g, (bagPage + 1) + "/" + Math.max(1, (int)Math.ceil(toDraw.size()/10.0)), 210, 450);

        // Left/Right Arrows
        bagLeftButton.drawArrow(g, Direction.LEFT);
        bagRightButton.drawArrow(g, Direction.RIGHT);

        // Back Arrow
        view.drawBackButton(g);

        for (Button button : bagButtons) {
            button.draw(g);
        }
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

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 12);

        g.drawString(itemNamesies.getName(), 28, 19);
        TextUtils.drawRightAlignedString(g, "x" + Game.getPlayer().getBag().getQuantity(itemNamesies), 140, 19);

        g.translate(-dx, -dy);
    }

    @Override
    public void update(BattleView view) {
        // Update all bag buttons and the back button
        view.setSelectedButton(bagButtons);

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

        Set<ItemNamesies> toDraw = bag.getCategory(BATTLE_BAG_CATEGORIES[selectedBagTab]);
        Iterator<ItemNamesies> iter = GeneralUtils.pageIterator(toDraw, bagPage, ITEMS_PER_PAGE);

        // Go through each item on the page
        for (int i = ITEMS; i < ITEMS + ITEMS_PER_PAGE && iter.hasNext(); i++) {
            ItemNamesies item = iter.next();
            if (bagButtons[i].checkConsumePress()) {
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
            bagPage = GeneralUtils.wrapIncrement(bagPage, increment, totalPages(toDraw));
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
}
