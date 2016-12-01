package gui.view.battle.handler;

import gui.Button;
import gui.TileSet;
import gui.view.View;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import input.ControlKey;
import item.ItemNamesies;
import item.bag.Bag;
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

    @Override
    public void set(BattleView view) {
        int pageSize = view.currentBattle.getPlayer().getBag().getCategory(BattleView.BATTLE_BAG_CATEGORIES[view.selectedBagTab]).size();

        for (int i = 0; i < BattleView.ITEMS_PER_PAGE; i++) {
            view.bagButtons[BattleView.ITEMS + i].setActive(i < pageSize - view.bagPage*BattleView.ITEMS_PER_PAGE);
        }

        // TODO: Make a method for this
        view.bagLastUsedBtn.setActive(view.currentBattle.getPlayer().getBag().getLastUsedItem() != ItemNamesies.NO_ITEM);

        for (Button b: view.bagButtons) {
            b.setForceHover(false);
        }

    }

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        g.drawImage(tiles.getTile(0x10), 0, 160, null);
        g.drawImage(tiles.getTile(BattleView.BATTLE_BAG_CATEGORIES[view.selectedBagTab].getImageNumber()), 30, 190, null);
        g.drawImage(tiles.getTile(BattleView.BATTLE_BAG_CATEGORIES[view.selectedBagTab].getImageNumber() - 4), 30, 492, null);
        g.drawImage(tiles.getTile(0x20), 415, 440, null);

        Bag bag = view.currentBattle.getPlayer().getBag();

        Set<ItemNamesies> toDraw = bag.getCategory(BattleView.BATTLE_BAG_CATEGORIES[view.selectedBagTab]);
        TileSet itemTiles = Game.getData().getItemTiles();

        DrawUtils.setFont(g, 12);
        Iterator<ItemNamesies> iter = toDraw.iterator();
        for (int i = 0; i < view.bagPage*BattleView.ITEMS_PER_PAGE; i++) {
            iter.next();
        }

        for (int y = 0; y < BattleView.ITEMS_PER_PAGE/2; y++) {
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
                DrawUtils.drawRightAlignedString(g, "x" + view.currentBattle.getPlayer().getBag().getQuantity(item), 140, 19);

                g.translate(-dx, -dy);
            }
        }

        // Bag page number
        DrawUtils.setFont(g, 20);
        DrawUtils.drawCenteredWidthString(g, (view.bagPage + 1) + "/" + Math.max(1, (int)Math.ceil(toDraw.size()/10.0)), 210, 450);

        // Left/Right Arrows
        View.drawArrows(g, view.bagLeftButton, view.bagRightButton);

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

        for (Button b: view.bagButtons) {
            b.draw(g);
        }

        view.backButton.draw(g);
    }

    @Override
    public void update(BattleView view) {
        // Update all bag buttons and the back button
        view.selectedButton = Button.update(view.bagButtons, view.selectedButton);
        view.backButton.update(false, ControlKey.BACK);

        // Check tabs
        for (int i = 0; i < BattleView.BATTLE_BAG_CATEGORIES.length; i++) {
            if (view.bagTabButtons[i].checkConsumePress()) {
                view.bagPage = 0;
                view.selectedBagTab = i;
                view.setVisualState(view.state); // To update active buttons
            }
        }

        CharacterData player = view.currentBattle.getPlayer();
        Bag bag = player.getBag();
        Set<ItemNamesies> toDraw = bag.getCategory(BattleView.BATTLE_BAG_CATEGORIES[view.selectedBagTab]);
        Iterator<ItemNamesies> iter = toDraw.iterator();

        // Skip ahead to the current page
        for (int i = 0; i < view.bagPage*BattleView.ITEMS_PER_PAGE; i++) {
            iter.next();
        }

        // Go through each item on the page
        for (int i = BattleView.ITEMS; i < BattleView.ITEMS + BattleView.ITEMS_PER_PAGE && iter.hasNext(); i++) {
            ItemNamesies item = iter.next();
            if (view.bagButtons[i].checkConsumePress()) {
                // Pokemon Use Item -- Set item to be selected an change to Pokemon View
                if (item.getItem() instanceof PokemonUseItem) {
                    view.selectedItem = item;
                    view.setVisualState(VisualState.USE_ITEM);
                    break;
                }
                // Otherwise, just use it on the battle if successful
                else if (bag.battleUseItem(item, view.currentBattle.getPlayer().front(), view.currentBattle)) {
                    view.currentBattle.getPlayer().performAction(view.currentBattle, Action.ITEM);
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
        if (view.bagLastUsedBtn.checkConsumePress()) {
            ItemNamesies lastItemUsed = bag.getLastUsedItem();
            if (lastItemUsed != ItemNamesies.NO_ITEM && bag.battleUseItem(lastItemUsed, player.front(), view.currentBattle)) {
                player.performAction(view.currentBattle, Action.ITEM);
                view.setVisualState(VisualState.MENU);
                view.cycleMessage(false);
            }
            else {
                view.cycleMessage(false);
                view.setVisualState(VisualState.INVALID_BAG);
            }
        }

        // Next page
        if (view.bagRightButton.checkConsumePress()) {
            // TODO: Should have a method to get the max pages also this should just use mod right?
            if (view.bagPage == ((int)Math.ceil(toDraw.size()/(double)BattleView.ITEMS_PER_PAGE) - 1)) {
                view.bagPage = 0;
            }
            else {
                view.bagPage++;
            }

            view.setVisualState(view.state); // To update active buttons
        }

        // Previous Page
        if (view.bagLeftButton.checkConsumePress()) {
            if (view.bagPage == 0) {
                view.bagPage = ((int)Math.ceil(toDraw.size()/(double)BattleView.ITEMS_PER_PAGE) - 1);
            }
            else {
                view.bagPage--;
            }

            view.setVisualState(view.state); // To update active buttons
        }

        // Return to main battle menu
        if (view.backButton.checkConsumePress()) {
            view.setVisualState(VisualState.MENU);
        }
    }
}
