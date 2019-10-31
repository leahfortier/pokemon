package gui.view.item;

import draw.button.Button;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.layout.DrawLayout;
import draw.panel.DrawPanel;
import item.ItemNamesies;
import main.Game;
import main.Global;
import map.Direction;

import java.util.List;

public class MartLayout extends BagLayout {
    public final DrawPanel amountPanel;
    public final DrawPanel playerMoneyPanel;
    public final DrawPanel inBagPanel;
    public final DrawPanel totalAmountPanel;
    private final DrawPanel confirmPanel;
    private final Button[] fakeAmountTabs;

    public MartLayout(boolean includeQuantity) {
        super(includeQuantity);

        DrawPanel[] leftPanels = new DrawLayout(super.leftPanel, 6, 1, 10)
                .withDrawSetup(panel -> panel.withBlackOutline().withLabelSize(18))
                .getPanels();
        playerMoneyPanel = leftPanels[0];
        inBagPanel = leftPanels[1];
        totalAmountPanel = leftPanels[4];
        confirmPanel = leftPanels[5];

        // Left and right arrow + centered amount panel
        fakeAmountTabs = getSelectedButtonLayout(3).getTabs();
        amountPanel = fakeAmountTabs[1].panel()
                                       .withOutlines(List.of(Direction.DOWN))
                                       .withLabelSize(20);
    }

    // Item amount, player money, in bag display, and total amount display labels
    public void setup(ItemNamesies selectedItem, int itemAmount, int totalAmount) {
        amountPanel.withLabel(Integer.toString(itemAmount));
        playerMoneyPanel.withLabel("Money: " + Global.MONEY_SYMBOL + Game.getPlayer().getDatCashMoney());
        inBagPanel.withLabel("In Bag: " + Game.getPlayer().getBag().getQuantity(selectedItem));
        totalAmountPanel.withLabel("Total: " + Global.MONEY_SYMBOL + totalAmount);
    }

    public Button createAmountArrowButton(Direction arrowDirection,
                                          ButtonTransitions transitions,
                                          ButtonPressAction pressAction) {
        return new Button(
                fakeAmountTabs[arrowDirection == Direction.LEFT ? 0 : 2].panel(),
                transitions,
                pressAction,
                panel -> panel.asArrow(arrowDirection, 35, 20)
                              .greyInactive()
                              .withBlackOutline()
                              .withBorderlessTransparentBackground()
                              .withBlackOutline()
        );
    }

    public Button createConfirmButton(String label, ButtonTransitions transitions, ButtonPressAction pressAction) {
        return new Button(
                confirmPanel,
                transitions,
                pressAction,
                panel -> panel.greyInactive()
                              .withLabel(label, 24)
                              .withBorderlessTransparentBackground()
                              .withBlackOutline()
        );
    }
}
