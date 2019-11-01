package gui.view.battle.handler;

import battle.Battle;
import draw.Alignment;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonPanel;
import draw.button.ButtonTransitions;
import draw.layout.ButtonLayout;
import draw.layout.DrawLayout;
import draw.layout.TabLayout;
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
import java.util.Map.Entry;
import java.util.Set;

public class BagState implements VisualStateHandler {

    private static final BattleBagCategory[] BATTLE_BAG_CATEGORIES = BattleBagCategory.values();
    private static final int ITEMS_PER_PAGE = 10;

    // Bag Button Indexes
    private static final int NUM_BUTTONS = BATTLE_BAG_CATEGORIES.length + ITEMS_PER_PAGE + 3;
    private static final int TABS = 0;
    private static final int ITEMS = TABS + BATTLE_BAG_CATEGORIES.length;
    private static final int BOTTOM_ITEM = ITEMS + ITEMS_PER_PAGE - 1;
    private static final int LAST_ITEM_USED = NUM_BUTTONS - 1;
    private static final int RIGHT_ARROW = NUM_BUTTONS - 2;
    private static final int LEFT_ARROW = NUM_BUTTONS - 3;

    private final PanelList panels;

    private final DrawPanel bagCategoryPanel;
    private final WrapPanel lastItemPanel;
    private final DrawPanel lastItemLabelPanel;

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

        lastItemPanel = new WrapPanel(bagCategoryPanel.x, 492, bagCategoryPanel.width, 78, 13)
                .withBorderPercentage(17)
                .withTransparentBackground()
                .withBlackOutline();

        tabButtons = new TabLayout(bagCategoryPanel, BATTLE_BAG_CATEGORIES.length, 28)
                .withStartIndex(TABS)
                .withDefaultTransitions(new ButtonTransitions().up(LAST_ITEM_USED).down(ITEMS))
                .withButtonSetup((panel, index) -> panel.withBorderSize(0)
                                                        .withBackgroundColor(BATTLE_BAG_CATEGORIES[index].getColor())
                                                        .withLabel(BATTLE_BAG_CATEGORIES[index].getName(), 18))
                .getTabs();

        ButtonLayout itemsLayout = new ButtonLayout(bagCategoryPanel, ITEMS_PER_PAGE/2, 2, 8)
                .withMissingBottomRow()
                .withStartIndex(ITEMS)
                .withDefaultTransitions(new ButtonTransitions().up(TABS).down(RIGHT_ARROW))
                .withButtonSetup(panel -> panel.asItemPanel(true).skipInactive());

        itemButtons = itemsLayout.getButtons();

        Entry<DrawPanel, DrawPanel> arrowPanels = itemsLayout.getArrowPanels();
        leftButton = new Button(
                arrowPanels.getKey(),
                new ButtonTransitions().right(RIGHT_ARROW).up(BOTTOM_ITEM - 1).down(LAST_ITEM_USED).left(RIGHT_ARROW)
        ).asArrow(Direction.LEFT);

        rightButton = new Button(
                arrowPanels.getValue(),
                new ButtonTransitions().up(BOTTOM_ITEM).left(LEFT_ARROW).down(LAST_ITEM_USED).right(LEFT_ARROW)
        ).asArrow(Direction.RIGHT);

        DrawPanel[] lastUsedPanels = new DrawLayout(lastItemPanel, 1, 2, itemButtons[0]).getPanels();
        lastItemLabelPanel = lastUsedPanels[0].withNoBackground()
                                              .withLabel("Last item used:", 16, Alignment.LEFT);

        lastUsedButton = new Button(
                lastUsedPanels[1],
                new ButtonTransitions().up(LEFT_ARROW).down(selectedBagTab),
                () -> {}, // Handled in update
                panel -> panel.skipInactive()
                              .asItemPanel(true)
        );

        this.buttons = new ButtonList(NUM_BUTTONS);
        this.buttons.set(TABS, tabButtons);
        this.buttons.set(ITEMS, itemButtons);
        this.buttons.set(LEFT_ARROW, leftButton);
        this.buttons.set(RIGHT_ARROW, rightButton);
        this.buttons.set(LAST_ITEM_USED, lastUsedButton);

        this.panels = new PanelList(bagCategoryPanel, lastItemPanel, lastItemLabelPanel);
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

        // Tab outlines
        for (int i = 0; i < BATTLE_BAG_CATEGORIES.length; i++) {
            this.tabButtons[i].panel().withTabOutlines(i, selectedBagTab);
        }

        // Show item description if a item button is currently highlighted instead of the last move used
        ItemNamesies highlighted = this.getHighlighted();
        if (highlighted == null) {
            ItemNamesies lastUsedItem = Game.getPlayer().getBag().getLastUsedItem();
            if (lastUsedItem != ItemNamesies.NO_ITEM) {
                lastUsedButton.panel().withItem(lastUsedItem);
            }
        } else {
            lastUsedButton.panel().skipDraw();
            lastItemLabelPanel.skipDraw();
        }

        List<ItemNamesies> items = GeneralUtils.pageValues(this.getDisplayItems(), bagPage, ITEMS_PER_PAGE);
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            ButtonPanel panel = itemButtons[i].panel();
            if (i < items.size()) {
                panel.withItem(items.get(i));
            } else {
                panel.skipDraw();
            }
        }
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

        // Show last item used if no item is selected
        ItemNamesies highlighted = this.getHighlighted();
        if (highlighted != null) {
            // Otherwise, draw selected item's information
            drawItemDescription(g, highlighted);
        }

        // Bag page numbers and arrows
        TextUtils.drawPageNumbers(g, 18, leftButton, rightButton, bagPage, totalPages());

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

    public WrapMetrics drawItemDescription(Graphics g, ItemNamesies itemNamesies) {
        return lastItemPanel.drawMessage(g, itemNamesies.getItem().getDescription());
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
