package gui.view.bag;

import draw.button.Button;
import draw.button.ButtonTransitions;
import draw.panel.DrawPanel;
import draw.panel.DrawPanel.ButtonIndexAction;
import item.bag.BagCategory;
import main.Global;
import map.Direction;
import util.Point;

import java.util.EnumSet;

public class BagPanel {
    private static final BagCategory[] CATEGORIES = BagCategory.values();
    private static final int ITEMS_PER_PAGE = 10;

    public final DrawPanel bagPanel;
    public final DrawPanel pokemonPanel;
    public final DrawPanel itemsPanel;
    public final DrawPanel selectedPanel;

    public final DrawPanel[] tabPanels;
    public final DrawPanel[] buttonPanels;
    public final DrawPanel returnPanel;

    public final DrawPanel leftArrow;
    public final DrawPanel rightArrow;

    public BagPanel() {
        int tabHeight = 55;
        int spacing = 28;

        bagPanel = new DrawPanel(
                spacing,
                spacing + tabHeight,
                Point.subtract(
                        Global.GAME_SIZE,
                        2*spacing,
                        2*spacing + tabHeight
                )
        )
                .withTransparentBackground()
                .withBorderPercentage(0)
                .withBlackOutline(EnumSet.complementOf(EnumSet.of(Direction.UP)));

        int buttonHeight = 38;
        int selectedHeight = 82;
        int halfPanelWidth = (bagPanel.width - 3*spacing)/2;

        pokemonPanel = new DrawPanel(
                bagPanel.x + spacing,
                bagPanel.y + spacing,
                halfPanelWidth,
                bagPanel.height - 2*spacing
        )
                .withFullTransparency()
                .withBlackOutline();

        selectedPanel = new DrawPanel(
                pokemonPanel.rightX() + spacing,
                bagPanel.y + spacing,
                halfPanelWidth,
                selectedHeight
        )
                .withFullTransparency()
                .withBlackOutline();

        itemsPanel = new DrawPanel(
                selectedPanel.x,
                selectedPanel.bottomY() + buttonHeight + spacing,
                halfPanelWidth,
                pokemonPanel.height - selectedPanel.height - 2*buttonHeight - 2*spacing
        )
                .withFullTransparency()
                .withBlackOutline();

        tabPanels = new DrawPanel[CATEGORIES.length];
        for (int i = 0; i < tabPanels.length; i++) {
            tabPanels[i] = bagPanel.createTab(i, tabHeight, tabPanels.length);
        }

        buttonPanels = new DrawPanel[UseState.values().length];
        for (int i = 0; i < buttonPanels.length; i++) {
            buttonPanels[i] = selectedPanel.createBottomTab(i, buttonHeight, buttonPanels.length);
        }

        // Fake buttons are fake (just used for spacing)
        Button[] itemButtons = this.getItemButtons(0, new ButtonTransitions(), index -> {});

        int arrowHeight = 20;
        leftArrow = new DrawPanel(
                itemsPanel.x + itemsPanel.width/4,
                itemButtons[itemButtons.length - 1].centerY() + (itemButtons[2].y - itemButtons[0].y) - arrowHeight/2,
                35,
                arrowHeight
        );

        rightArrow = new DrawPanel(
                itemsPanel.rightX() - (leftArrow.x - itemsPanel.x) - leftArrow.width,
                leftArrow.y,
                leftArrow.width,
                leftArrow.height
        );

        returnPanel = new DrawPanel(
                selectedPanel.x,
                bagPanel.bottomY() - spacing - buttonHeight,
                halfPanelWidth,
                buttonHeight
        );
    }

    public Button[] getItemButtons(int startIndex, ButtonTransitions defaultTransitions, ButtonIndexAction indexAction) {
        return itemsPanel.getButtons(
                5,
                ITEMS_PER_PAGE/2 + 1,
                2,
                ITEMS_PER_PAGE/2,
                2,
                startIndex,
                defaultTransitions,
                indexAction
        );
    }
}
