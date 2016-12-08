package gui.view.battle.handler;

import battle.Battle;
import gui.TileSet;
import gui.button.Button;
import gui.button.ButtonHoverAction;
import gui.panel.DrawPanel;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import item.ItemNamesies;
import item.bag.Bag;
import item.bag.BattleBagCategory;
import item.use.PokemonUseItem;
import main.Game;
import map.Direction;
import trainer.CharacterData;
import trainer.Trainer.Action;
import util.DrawUtils;
import util.FontMetrics;

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
                    });
        }

        bagButtons[BAG_LEFT_BUTTON] = bagLeftButton = new Button(135, 435, 35, 20, ButtonHoverAction.BOX, new int[] {BAG_RIGHT_BUTTON, ITEMS + ITEMS_PER_PAGE - 2, -1, LAST_ITEM_BUTTON});
        bagButtons[BAG_RIGHT_BUTTON] = bagRightButton = new Button(250,435,35,20, ButtonHoverAction.BOX, new int[] {-1, ITEMS + ITEMS_PER_PAGE - 1, BAG_LEFT_BUTTON, LAST_ITEM_BUTTON});
        bagButtons[LAST_ITEM_BUTTON] = bagLastUsedBtn = new Button(214, 517, 148, 28, ButtonHoverAction.BOX, new int[] {-1, BAG_LEFT_BUTTON, -1, selectedBagTab});

        for (int y = 0, i = ITEMS; y < ITEMS_PER_PAGE/2; y++) {
            for (int x = 0; x < 2; x++, i++) {
                bagButtons[i] = new Button(
                        55 + x*162,
                        243 + y*38,
                        148,
                        28,
                        ButtonHoverAction.BOX,
                        new int[] {
                                (i + 1 - ITEMS)%ITEMS_PER_PAGE + ITEMS, // Right
                                y == 0 ? selectedBagTab : i - 2, // Up
                                (i - 1 - ITEMS + ITEMS_PER_PAGE)%ITEMS_PER_PAGE + ITEMS, // Left
                                y == ITEMS_PER_PAGE/2 - 1 ? (x == 0 ? BAG_LEFT_BUTTON : BAG_RIGHT_BUTTON) : i + 2 // Down
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

        for (Button button: bagButtons) {
            button.setForceHover(false);
        }
    }

    @Override
    public void draw(BattleView view, Graphics g) {
        view.drawLargeMenuPanel(g);

        BattleBagCategory selectedCategory = BATTLE_BAG_CATEGORIES[selectedBagTab];
        bagCategoryPanel
                .withTransparentBackground(selectedCategory.getColor())
                .withBlackOutline(EnumSet.complementOf(EnumSet.of(Direction.UP)))
                .drawBackground(g);

        lastItemPanel
                .withTransparentBackground(selectedCategory.getColor())
                .drawBackground(g);

        // Tabs
        for (int i = 0; i < BATTLE_BAG_CATEGORIES.length; i++) {
            Button tabButton = bagTabButtons[i];
            BattleBagCategory category = BATTLE_BAG_CATEGORIES[i];

            tabButton.fill(g, category.getColor());
            tabButton.label(g, 18, category.getName());
            tabButton.outlineTab(g, i, selectedBagTab);
        }

        DrawUtils.blackOutline(g, 30, 190, 357, 287);

        // Messages text
        String message = view.getMessage(VisualState.INVALID_BAG, "Choose an item!");
        view.drawMenuMessagePanel(g, message);

        Bag bag = Game.getPlayer().getBag();

        Set<ItemNamesies> toDraw = bag.getCategory(BATTLE_BAG_CATEGORIES[selectedBagTab]);
        TileSet itemTiles = Game.getData().getItemTiles();

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 12);
        Iterator<ItemNamesies> iter = toDraw.iterator();
        for (int i = 0; i < bagPage*ITEMS_PER_PAGE; i++) {
            iter.next();
        }

        for (int i = 0; i < ITEMS_PER_PAGE && iter.hasNext(); i++) {
            drawItemButton(g, itemTiles, bagButtons[ITEMS + i], iter.next());
        }

        // Bag page number
        FontMetrics.setFont(g, 20);
        DrawUtils.drawCenteredWidthString(g, (bagPage + 1) + "/" + Math.max(1, (int)Math.ceil(toDraw.size()/10.0)), 210, 450);

        // Left/Right Arrows
        bagLeftButton.drawArrow(g, Direction.LEFT);
        bagRightButton.drawArrow(g, Direction.RIGHT);

        // Last Item Used
        ItemNamesies lastUsedItem = bag.getLastUsedItem();
        lastItemPanel.drawLeftLabel(g, 16, "Last item used:");

        // TODO: Should have a method to check if it is the empty item
        if (lastUsedItem != ItemNamesies.NO_ITEM) {
            drawItemButton(g, itemTiles, bagLastUsedBtn, lastUsedItem);
        }

        // Back Arrow
        view.drawBackButton(g);

        for (Button button: bagButtons) {
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

        BufferedImage img = itemTiles.getTile(itemNamesies.getItem().getImageIndex());
        DrawUtils.drawCenteredImage(g, img, 14, 14);

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 12);

        g.drawString(itemNamesies.getName(), 28, 19);
        DrawUtils.drawRightAlignedString(g, "x" + Game.getPlayer().getBag().getQuantity(itemNamesies), 140, 19);

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
        CharacterData player = Game.getPlayer();
        Bag bag = player.getBag();
        Set<ItemNamesies> toDraw = bag.getCategory(BATTLE_BAG_CATEGORIES[selectedBagTab]);
        Iterator<ItemNamesies> iter = toDraw.iterator();

        // Skip ahead to the current page
        for (int i = 0; i < bagPage*ITEMS_PER_PAGE; i++) {
            iter.next();
        }

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
                    player.performAction(currentBattle, Action.ITEM);
                    view.setVisualState(VisualState.MENU);
                    view.cycleMessage(false);
                    break;
                }
                // If the item cannot be used, do not consume
                else {
                    view.cycleMessage(false);
                    view.setVisualState(VisualState.INVALID_BAG);
                }
            }
        }

        // Selecting the Last Item Used Button
        if (bagLastUsedBtn.checkConsumePress()) {
            ItemNamesies lastItemUsed = bag.getLastUsedItem();
            if (lastItemUsed != ItemNamesies.NO_ITEM && bag.battleUseItem(lastItemUsed, player.front(), currentBattle)) {
                player.performAction(currentBattle, Action.ITEM);
                view.setVisualState(VisualState.MENU);
                view.cycleMessage(false);
            }
            else {
                view.cycleMessage(false);
                view.setVisualState(VisualState.INVALID_BAG);
            }
        }

        // Next page
        if (bagRightButton.checkConsumePress()) {
            // TODO: Should have a method to get the max pages also this should just use mod right?
            if (bagPage == ((int)Math.ceil(toDraw.size()/(double)ITEMS_PER_PAGE) - 1)) {
                bagPage = 0;
            }
            else {
                bagPage++;
            }

            view.setVisualState(); // To update active buttons
        }

        // Previous Page
        if (bagLeftButton.checkConsumePress()) {
            if (bagPage == 0) {
                bagPage = ((int)Math.ceil(toDraw.size()/(double)ITEMS_PER_PAGE) - 1);
            }
            else {
                bagPage--;
            }

            view.setVisualState(); // To update active buttons
        }

        // Return to main battle menu
        view.updateBackButton();
    }

    public ItemNamesies getSelectedItem() {
        return this.selectedItem;
    }
}
