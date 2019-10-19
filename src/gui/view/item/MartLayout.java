package gui.view.item;

import draw.DrawUtils;
import draw.button.Button;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.panel.DrawPanel;
import item.ItemNamesies;
import main.Game;
import main.Global;
import map.Direction;

public class MartLayout extends BagLayout {
    public final DrawPanel amountPanel;
    public final DrawPanel playerMoneyPanel;
    public final DrawPanel inBagPanel;
    public final DrawPanel totalAmountPanel;
    private final DrawPanel confirmPanel;

    public MartLayout(boolean includeQuantity) {
        super(includeQuantity);

        Button[] fakeButtons = super.leftPanel.getButtons(10, 6, 1);
        playerMoneyPanel = new DrawPanel(fakeButtons[0])
                .withBlackOutline()
                .withLabelSize(18);
        inBagPanel = new DrawPanel(fakeButtons[1])
                .withBlackOutline()
                .withLabelSize(18);
        totalAmountPanel = new DrawPanel(fakeButtons[4])
                .withBlackOutline()
                .withLabelSize(18);
        confirmPanel = new DrawPanel(fakeButtons[5]);

        DrawPanel amountLeftButton = selectedButtonPanels[0];
        DrawPanel amountRightButton = selectedButtonPanels[2];
        amountPanel = new DrawPanel(
                amountLeftButton.x + amountLeftButton.width - DrawUtils.OUTLINE_SIZE,
                amountLeftButton.y,
                selectedPanel.width - amountLeftButton.width - amountRightButton.width + 2*DrawUtils.OUTLINE_SIZE,
                amountLeftButton.height
        )
                .withFullTransparency()
                .withBlackOutline()
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
                selectedButtonPanels[arrowDirection == Direction.LEFT ? 0 : 2],
                transitions,
                pressAction,
                panel -> panel.asArrow(arrowDirection, 35, 20)
                              .greyInactive()
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
