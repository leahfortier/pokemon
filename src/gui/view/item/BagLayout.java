package gui.view.item;

import draw.Alignment;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.layout.ArrowLayout;
import draw.layout.ButtonLayout;
import draw.layout.ButtonLayout.ButtonIndexAction;
import draw.layout.TabLayout;
import draw.panel.DrawPanel;
import draw.panel.ItemPanel;
import draw.panel.WrapPanel.WrapMetrics;
import item.ItemNamesies;
import item.bag.BagCategory;
import main.Global;
import map.Direction;
import util.GeneralUtils;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class BagLayout {
    private static final BagCategory[] CATEGORIES = BagCategory.values();
    private static final int NUM_ITEM_ROWS = 5;
    private static final int NUM_ITEM_COLS = 2;
    private static final int ITEMS_PER_PAGE = NUM_ITEM_ROWS*NUM_ITEM_COLS;
    private static final int TAB_HEIGHT = 55;
    private static final int BUTTON_HEIGHT = 38;

    public final int spacing = 28;

    public final DrawPanel bagPanel;
    public final DrawPanel leftPanel;
    public final DrawPanel itemsPanel;
    public final ItemPanel selectedPanel;

    public final DrawPanel leftArrow;
    public final DrawPanel rightArrow;
    private final DrawPanel returnPanel;

    private final boolean includeQuantity;

    public BagLayout(boolean includeQuantity) {
        this.includeQuantity = includeQuantity;

        bagPanel = new DrawPanel(
                spacing,
                spacing + TAB_HEIGHT,
                Point.subtract(
                        Global.GAME_SIZE,
                        2*spacing,
                        2*spacing + TAB_HEIGHT
                )
        )
                .withFullTransparency()
                .withBlackOutline();

        int selectedHeight = 82;
        int halfPanelWidth = (bagPanel.width - 3*spacing)/2;

        leftPanel = new DrawPanel(
                bagPanel.x + spacing,
                bagPanel.y + spacing,
                halfPanelWidth,
                bagPanel.height - 2*spacing
        )
                .withFullTransparency()
                .withBlackOutline();

        selectedPanel = new ItemPanel(
                leftPanel.rightX() + spacing,
                bagPanel.y + spacing,
                halfPanelWidth,
                selectedHeight,
                includeQuantity
        );

        itemsPanel = new DrawPanel(
                selectedPanel.x,
                selectedPanel.bottomY() + BUTTON_HEIGHT + spacing,
                halfPanelWidth,
                leftPanel.height - selectedPanel.height - 2*BUTTON_HEIGHT - 2*spacing
        )
                .withFullTransparency()
                .withBlackOutline();

        ArrowLayout arrowPanels = this.getItemsLayout().getArrowLayout();
        leftArrow = arrowPanels.getLeftPanel();
        rightArrow = arrowPanels.getRightPanel();

        returnPanel = new DrawPanel(
                selectedPanel.x,
                bagPanel.bottomY() - spacing - BUTTON_HEIGHT,
                halfPanelWidth,
                BUTTON_HEIGHT
        );
    }

    public TabLayout getSelectedButtonLayout(int numButtons) {
        return new TabLayout(selectedPanel, numButtons, BUTTON_HEIGHT)
                .withButtonSetup((panel, index) -> panel.withBlackOutline()
                                                        .withFullTransparency())
                .asBottomTabs();
    }

    private ButtonLayout getItemsLayout() {
        return new ButtonLayout(itemsPanel, NUM_ITEM_ROWS, NUM_ITEM_COLS, 5)
                .withMissingBottomRow()
                .withButtonSetup(panel -> panel.asItemPanel(includeQuantity));
    }

    public Button[] getItemButtons(int startIndex,
                                   ButtonTransitions defaultTransitions,
                                   ButtonIndexAction indexAction) {
        return this.getItemsLayout()
                .withStartIndex(startIndex)
                .withDefaultTransitions(defaultTransitions)
                .withPressIndex(indexAction)
                .getButtons();
    }

    public TabLayout getTabs() {
        return new TabLayout(bagPanel, CATEGORIES.length, TAB_HEIGHT)
                .withButtonSetup((panel, index) -> {
                    final BagCategory category = CATEGORIES[index];
                    panel.withFullTransparency()
                         .withBackgroundColor(category.getColor())
                         .withLabelSize(14, Alignment.LEFT)
                         .withLabelSpacingFactor(1/3f)
                         .withImageLabel(category.getIcon(), category.getDisplayName());
                });
    }

    public DrawPanel getTabPanel(int index, Color color, String label) {
        DrawPanel tabPanel = this.getTabs().getTabs()[index].panel();
        return tabPanel.withFullTransparency()
                       .withBackgroundColor(color)
                       .withMissingBlackOutline(Direction.DOWN)
                       .withLabel(label, 16);
    }

    public Button[] getTabButtons(int startIndex, int upIndex, int downIndex, ButtonIndexAction indexAction) {
        return this.getTabs()
                   .withStartIndex(startIndex)
                   .withDefaultTransitions(new ButtonTransitions().up(upIndex).down(downIndex))
                   .withPressIndex(indexAction)
                   .getTabs();
    }

    public void setupTabs(Button[] tabButtons, BagCategory selectedTab) {
        // Use tab color for background
        bagPanel.withBackgroundColor(selectedTab.getColor());

        // Give correct outline for each tab
        for (int i = 0; i < CATEGORIES.length; i++) {
            tabButtons[i].panel().withTabOutlines(i, selectedTab.ordinal());
        }
    }

    public WrapMetrics drawSelectedItem(Graphics g, ItemNamesies selectedItem) {
        return this.selectedPanel.drawMessage(g, selectedItem);
    }

    // Sets up active and draw for item buttons
    public void setupItems(Button[] itemButtons, Iterable<ItemNamesies> items, int pageNum) {
        List<ItemNamesies> pageItems = GeneralUtils.pageValues(items, pageNum, ITEMS_PER_PAGE);
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            Button button = itemButtons[i];
            button.setActiveSkip(i < pageItems.size());
            if (button.isActive()) {
                button.panel().withItem(pageItems.get(i));
            }
        }
    }

    public void drawPageNumbers(Graphics g, int pageNum, int totalPages) {
        TextUtils.drawPageNumbers(g, 16, leftArrow, rightArrow, pageNum, totalPages);
    }

    public Button createReturnButton(ButtonTransitions transitions) {
        return createReturnButton(transitions, ButtonPressAction.getExitAction());
    }

    public Button createReturnButton(ButtonTransitions transitions, ButtonPressAction pressAction) {
        return new Button(
                this.returnPanel,
                transitions,
                pressAction,
                panel -> panel.withTransparentBackground()
                              .withBlackOutline()
                              .withLabel("Return", 20)
        );
    }
}
