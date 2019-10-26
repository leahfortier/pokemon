package gui.view.battle.handler;

import battle.Battle;
import draw.Alignment;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.panel.DrawPanel;
import draw.panel.PanelList;
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
import java.util.List;
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

    private final PanelList panels;

    private final DrawPanel bagCategoryPanel;
    private final DrawPanel lastItemPanel;
    private final WrapPanel itemDescriptionPanel;

    private final ButtonList buttons;
    private final Button[] tabButtons;
    private final Button[] itemButtons;
    private final Button rightButton;
    private final Button leftButton;
    private final Button lastUsedButton;

    // Current bag page, bag category, and selected item
    private int bagPage;
    private int selectedBagTab;
    private ItemNamesies selectedItem;

    public BagState() {
        bagCategoryPanel = new DrawPanel(30, 218, 357, 259)
                .withBorderPercentage(6)
                .withTransparentBackground()
                .withBlackOutline();

        lastItemPanel = new DrawPanel(bagCategoryPanel.x, 492, bagCategoryPanel.width, 78)
                .withBorderPercentage(17)
                .withTransparentBackground()
                .withBlackOutline()
                .withLabel("Last item used:", 16, Alignment.LEFT);

        itemDescriptionPanel = new WrapPanel(lastItemPanel, 13)
                .withBorderSize(lastItemPanel.getBorderSize())
                .withTransparentBackground()
                .withBlackOutline();

        // Bag View Buttons
        Button[] bagButtons = new Button[NUM_BAG_BUTTONS];

        tabButtons = new Button[BATTLE_BAG_CATEGORIES.length];
        for (int i = 0; i < BATTLE_BAG_CATEGORIES.length; i++) {
            BattleBagCategory category = BATTLE_BAG_CATEGORIES[i];
            bagButtons[i] = tabButtons[i] = new Button(
                    bagCategoryPanel.createTab(i, 28, tabButtons.length),
                    new ButtonTransitions()
                            .up(LAST_ITEM_BUTTON)
                            .down(ITEMS)
                            .basic(Direction.RIGHT, i, 1, BATTLE_BAG_CATEGORIES.length)
                            .basic(Direction.LEFT, i, 1, BATTLE_BAG_CATEGORIES.length),
                    () -> {}, // Handled in update
                    panel -> panel.withBorderSize(0)
                                  .withBackgroundColor(category.getColor())
                                  .withLabel(category.getName(), 18)
            );
        }

        bagButtons[BAG_LEFT_BUTTON] = leftButton = new Button(
                135, 435, 35, 20, ButtonHoverAction.BOX,
                new ButtonTransitions().right(BAG_RIGHT_BUTTON).up(ITEMS + ITEMS_PER_PAGE - 2).down(LAST_ITEM_BUTTON)
        ).asArrow(Direction.LEFT);

        bagButtons[BAG_RIGHT_BUTTON] = rightButton = new Button(
                250, 435, 35, 20, ButtonHoverAction.BOX,
                new ButtonTransitions().up(ITEMS + ITEMS_PER_PAGE - 1).left(BAG_LEFT_BUTTON).down(LAST_ITEM_BUTTON)
        ).asArrow(Direction.RIGHT);

        bagButtons[LAST_ITEM_BUTTON] = lastUsedButton = new Button(
                214, 517, 148, 28,
                new ButtonTransitions().up(BAG_LEFT_BUTTON).down(selectedBagTab),
                () -> {}, // Handled in update
                panel -> panel.skipInactive()
                              .withBlackOutline()
                              .withBackgroundColor(Color.WHITE)
                              .withBorderPercentage(0)
        );

        itemButtons = new Button[ITEMS_PER_PAGE];
        for (int y = 0, i = 0; y < ITEMS_PER_PAGE/2; y++) {
            for (int x = 0; x < 2; x++, i++) {
                itemButtons[i] = bagButtons[i + ITEMS] = new Button(
                        55 + x*162,
                        243 + y*38,
                        148,
                        28,
                        ButtonHoverAction.BOX,
                        new ButtonTransitions()
                                .right((i + 1)%ITEMS_PER_PAGE + ITEMS)
                                .up(y == 0 ? selectedBagTab : i + ITEMS - 2)
                                .left((i - 1 + ITEMS_PER_PAGE)%ITEMS_PER_PAGE + ITEMS)
                                .down(y == ITEMS_PER_PAGE/2 - 1 ? (x == 0 ? BAG_LEFT_BUTTON : BAG_RIGHT_BUTTON) : i + ITEMS + 2),
                        () -> {}, // Handled in update
                        panel -> panel.skipInactive()
                                      .withBlackOutline()
                                      .withBackgroundColor(Color.WHITE)
                                      .withBorderPercentage(0)
                );
            }
        }

        this.buttons = new ButtonList(bagButtons);
        this.panels = new PanelList(bagCategoryPanel, lastItemPanel, itemDescriptionPanel);
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
            itemButtons[i].setActive(i < pageSize - bagPage*ITEMS_PER_PAGE);
        }

        // TODO: Make a method for this
        lastUsedButton.setActive(playerBag.getLastUsedItem() != ItemNamesies.NO_ITEM);

        buttons.setFalseHover();
    }

    private void drawSetup() {
        Color tabColor = BATTLE_BAG_CATEGORIES[selectedBagTab].getColor();
        bagCategoryPanel.withBackgroundColor(tabColor);
        lastItemPanel.withBackgroundColor(tabColor);
        itemDescriptionPanel.withBackgroundColor(tabColor);

        // Tab outlines
        for (int i = 0; i < BATTLE_BAG_CATEGORIES.length; i++) {
            this.tabButtons[i].panel().withTabOutlines(i, selectedBagTab);
        }

        // Show item description if a item button is currently highlighted instead of the last move used
        boolean showDescription = this.getHighlighted() != null;
        lastItemPanel.skipDraw(showDescription);
        lastUsedButton.panel().skipDraw(showDescription);
        itemDescriptionPanel.skipDraw(!showDescription);
    }

    private ItemNamesies getHighlighted() {
        int selectedButton = buttons.getSelected();
        if (selectedButton >= ITEMS && selectedButton < ITEMS + ITEMS_PER_PAGE) {
            return GeneralUtils.getPageValue(this.getDisplayItems(), bagPage, ITEMS_PER_PAGE, selectedButton - ITEMS);
        }

        // Item button is not currently selected
        return null;
    }

    @Override
    public void draw(BattleView view, Graphics g) {
        drawSetup();

        // Background
        view.drawLargeMenuPanel(g);
        panels.drawAll(g);
        buttons.drawPanels(g);

        // Messages text
        String message = view.getMessage(VisualState.INVALID_BAG, "Choose an item!");
        view.drawMenuMessagePanel(g, message);

        Bag bag = Game.getPlayer().getBag();
        TileSet itemTiles = Game.getData().getItemTiles();

        List<ItemNamesies> items = GeneralUtils.pageValues(this.getDisplayItems(), bagPage, ITEMS_PER_PAGE);
        for (int i = 0; i < items.size(); i++) {
            ItemNamesies item = items.get(i);
            drawItemButton(g, itemTiles, itemButtons[i], item);
        }

        // Show last item used if no item is selected
        ItemNamesies highlighted = this.getHighlighted();
        if (highlighted == null) {
            // Last Item Used
            ItemNamesies lastUsedItem = bag.getLastUsedItem();
            if (lastUsedItem != ItemNamesies.NO_ITEM) {
                drawItemButton(g, itemTiles, lastUsedButton, lastUsedItem);
            }
        } else {
            // Otherwise, draw selected item's information
            drawItemDescription(g, highlighted);
        }

        // Bag page numbers and arrows
        TextUtils.drawPageNumbers(g, 20, leftButton, rightButton, bagPage, totalPages());

        // Back Arrow
        view.drawBackButton(g);

        buttons.drawHover(g);
    }

    private Set<ItemNamesies> getDisplayItems() {
        return Game.getPlayer().getBag().getCategory(BATTLE_BAG_CATEGORIES[selectedBagTab]);
    }

    private int totalPages() {
        return GeneralUtils.getTotalPages(this.getDisplayItems().size(), ITEMS_PER_PAGE);
    }

    private void drawItemButton(Graphics g, TileSet itemTiles, Button button, ItemNamesies itemNamesies) {
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
        return itemDescriptionPanel.drawMessage(g, itemNamesies.getItem().getDescription());
    }

    @Override
    public void update(BattleView view) {
        // Update all bag buttons and the back button
        buttons.update();

        // Check tabs
        for (int i = 0; i < BATTLE_BAG_CATEGORIES.length; i++) {
            if (tabButtons[i].checkConsumePress()) {
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
            if (itemButtons[i].checkConsumePress()) {
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
        if (lastUsedButton.checkConsumePress()) {
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
        if (rightButton.checkConsumePress()) {
            // Next page
            increment = 1;
        }
        if (leftButton.checkConsumePress()) {
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
        return this.buttons;
    }
}
