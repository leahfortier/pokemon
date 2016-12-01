package gui.view.battle.handler;

import battle.Battle;
import gui.Button;
import gui.TileSet;
import gui.view.View;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import input.ControlKey;
import item.ItemNamesies;
import item.bag.Bag;
import item.bag.BattleBagCategory;
import item.use.PokemonUseItem;
import main.Game;
import trainer.CharacterData;
import trainer.Trainer.Action;
import util.DrawUtils;

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
        // Bag View Buttons
        bagButtons = new Button[NUM_BAG_BUTTONS];

        bagTabButtons = new Button[BATTLE_BAG_CATEGORIES.length];
        for (int i = 0; i < BATTLE_BAG_CATEGORIES.length; i++) {
            bagButtons[i] = bagTabButtons[i] = new Button(
                    i*89 + 30,
                    190,
                    89,
                    28,
                    Button.HoverAction.BOX,
                    new int[] { (i + 1)% BATTLE_BAG_CATEGORIES.length, // Right
                            LAST_ITEM_BUTTON, // Up
                            (i - 1 + BATTLE_BAG_CATEGORIES.length)% BATTLE_BAG_CATEGORIES.length, // Left
                            ITEMS }  // Down
            );
        }

        bagButtons[BAG_LEFT_BUTTON] = bagLeftButton = new Button(135, 435, 35, 20, Button.HoverAction.BOX, new int[] {BAG_RIGHT_BUTTON, ITEMS + ITEMS_PER_PAGE - 2, -1, LAST_ITEM_BUTTON});
        bagButtons[BAG_RIGHT_BUTTON] = bagRightButton = new Button(250,435,35,20, Button.HoverAction.BOX, new int[] {-1, ITEMS + ITEMS_PER_PAGE - 1, BAG_LEFT_BUTTON, LAST_ITEM_BUTTON});
        bagButtons[LAST_ITEM_BUTTON] = bagLastUsedBtn = new Button(214, 517, 148, 28, Button.HoverAction.BOX, new int[] {-1, BAG_LEFT_BUTTON, -1, selectedBagTab});

        for (int y = 0, i = ITEMS; y < ITEMS_PER_PAGE/2; y++) {
            for (int x = 0; x < 2; x++, i++) {
                bagButtons[i] = new Button(
                        55 + x*162,
                        243 + y*38,
                        148,
                        28,
                        Button.HoverAction.BOX,
                        new int[] { (i + 1 - ITEMS)%ITEMS_PER_PAGE + ITEMS, // Right
                                y == 0 ? selectedBagTab : i - 2, // Up
                                (i - 1 - ITEMS + ITEMS_PER_PAGE)%ITEMS_PER_PAGE + ITEMS, // Left
                                y == ITEMS_PER_PAGE/2 - 1 ? (x == 0 ? BAG_LEFT_BUTTON : BAG_RIGHT_BUTTON) : i + 2 }
                ); // Down
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
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        g.drawImage(tiles.getTile(0x10), 0, 160, null);
        g.drawImage(tiles.getTile(BATTLE_BAG_CATEGORIES[selectedBagTab].getImageNumber()), 30, 190, null);
        g.drawImage(tiles.getTile(BATTLE_BAG_CATEGORIES[selectedBagTab].getImageNumber() - 4), 30, 492, null);
        g.drawImage(tiles.getTile(0x20), 415, 440, null);

        Bag bag = Game.getPlayer().getBag();

        Set<ItemNamesies> toDraw = bag.getCategory(BATTLE_BAG_CATEGORIES[selectedBagTab]);
        TileSet itemTiles = Game.getData().getItemTiles();

        DrawUtils.setFont(g, 12);
        Iterator<ItemNamesies> iter = toDraw.iterator();
        for (int i = 0; i < bagPage*ITEMS_PER_PAGE; i++) {
            iter.next();
        }

        for (int y = 0; y < ITEMS_PER_PAGE/2; y++) {
            for (int x = 0; x < 2 && iter.hasNext(); x++) {
                int dx = 55 + x*162, dy = 243 + y*38;

                g.translate(dx, dy);

                // Draw box
                g.drawImage(tiles.getTile(0x11), 0, 0, null);

                // Draw item image
                ItemNamesies item = iter.next();
                BufferedImage img = itemTiles.getTile(item.getItem().getImageIndex());
                DrawUtils.drawCenteredImage(g, img, 14, 14);

                // Item name
                g.drawString(item.getName(), 28, 19);

                // Item quantity
                DrawUtils.drawRightAlignedString(g, "x" + bag.getQuantity(item), 140, 19);

                g.translate(-dx, -dy);
            }
        }

        // Bag page number
        DrawUtils.setFont(g, 20);
        DrawUtils.drawCenteredWidthString(g, (bagPage + 1) + "/" + Math.max(1, (int)Math.ceil(toDraw.size()/10.0)), 210, 450);

        // Left/Right Arrows
        View.drawArrows(g, bagLeftButton, bagRightButton);

        // Last Item Used
        ItemNamesies lastUsedItem = bag.getLastUsedItem();

        // TODO: Should have a method to check if it is the empty item
        if (lastUsedItem != ItemNamesies.NO_ITEM) {
            g.translate(214, 517);
            DrawUtils.setFont(g, 12);
            g.drawImage(tiles.getTile(0x11), 0, 0, null);

            BufferedImage img = itemTiles.getTile(lastUsedItem.getItem().getImageIndex());
            DrawUtils.drawCenteredImage(g, img, 14, 14);

            g.drawString(lastUsedItem.getName(), 28, 19);
            DrawUtils.drawRightAlignedString(g, "x" + bag.getQuantity(lastUsedItem), 140, 19);

            g.translate(-214, -517);
        }

        // Messages text
        String msgLine = view.state == VisualState.INVALID_BAG && view.message != null ? view.message : "Choose an item!";
        g.setColor(Color.BLACK);
        DrawUtils.setFont(g, 30);
        DrawUtils.drawWrappedText(g, msgLine, 440, 495, 350);

        // Back Arrow
        View.drawArrows(g, null, view.backButton);

        for (Button b: bagButtons) {
            b.draw(g);
        }

        view.backButton.draw(g);
    }

    @Override
    public void update(BattleView view) {
        // Update all bag buttons and the back button
        view.selectedButton = Button.update(bagButtons, view.selectedButton);
        view.backButton.update(false, ControlKey.BACK);

        // Check tabs
        for (int i = 0; i < BATTLE_BAG_CATEGORIES.length; i++) {
            if (bagTabButtons[i].checkConsumePress()) {
                bagPage = 0;
                selectedBagTab = i;
                view.setVisualState(view.state); // To update active buttons
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

            view.setVisualState(view.state); // To update active buttons
        }

        // Previous Page
        if (bagLeftButton.checkConsumePress()) {
            if (bagPage == 0) {
                bagPage = ((int)Math.ceil(toDraw.size()/(double)ITEMS_PER_PAGE) - 1);
            }
            else {
                bagPage--;
            }

            view.setVisualState(view.state); // To update active buttons
        }

        // Return to main battle menu
        if (view.backButton.checkConsumePress()) {
            view.setVisualState(VisualState.MENU);
        }
    }

    public ItemNamesies getSelectedItem() {
        return this.selectedItem;
    }
}
