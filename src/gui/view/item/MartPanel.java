package gui.view.item;

import draw.DrawUtils;
import draw.PolygonUtils;
import draw.button.Button;
import draw.panel.DrawPanel;
import item.ItemNamesies;
import main.Game;
import main.Global;
import map.Direction;

import java.awt.Graphics;

public class MartPanel extends BagPanel {
    private final DrawPanel amountPanel;
    private final DrawPanel playerMoneyPanel;
    private final DrawPanel inBagPanel;
    private final DrawPanel totalAmountPanel;
    public final DrawPanel confirmPanel;

    public MartPanel() {
        super(false); // No quantities

        Button[] fakeButtons = super.leftPanel.getButtons(10, 6, 1);
        playerMoneyPanel = new DrawPanel(fakeButtons[0]).withBlackOutline();
        inBagPanel = new DrawPanel(fakeButtons[1]).withBlackOutline();
        totalAmountPanel = new DrawPanel(fakeButtons[4]).withBlackOutline();
        confirmPanel = new DrawPanel(fakeButtons[5]);

        DrawPanel amountLeftButton = buttonPanels[0];
        DrawPanel amountRightButton = buttonPanels[2];
        amountPanel = new DrawPanel(
                amountLeftButton.x + amountLeftButton.width - DrawUtils.OUTLINE_SIZE,
                amountLeftButton.y,
                selectedPanel.width - amountLeftButton.width - amountRightButton.width + 2*DrawUtils.OUTLINE_SIZE,
                amountLeftButton.height
        )
                .withFullTransparency()
                .withBlackOutline();
    }

    public void drawAmount(Graphics g, int itemAmount, Button leftArrow, Button rightArrow) {
        amountPanel.drawBackground(g);
        amountPanel.label(g, 20, itemAmount + "");

        this.drawAmountArrow(g, leftArrow, Direction.LEFT);
        this.drawAmountArrow(g, rightArrow, Direction.RIGHT);
    }

    private void drawAmountArrow(Graphics g, Button arrow, Direction arrowDirection) {
        if (!arrow.isActive()) {
            arrow.greyOut(g);
        }

        arrow.fillTransparent(g);
        arrow.blackOutline(g);
        PolygonUtils.drawCenteredArrow(g, arrow.centerX(), arrow.centerY(), 35, 20, arrowDirection);
    }

    public void drawMoneyPanel(Graphics g, ItemNamesies selectedItem, int total) {
        leftPanel.drawBackground(g);

        // Player Money
        playerMoneyPanel.drawBackground(g);
        playerMoneyPanel.label(g, 18, "Money: " + Global.MONEY_SYMBOL + Game.getPlayer().getDatCashMoney());

        // In bag display
        inBagPanel.drawBackground(g);
        inBagPanel.label(g, 18, "In Bag: " + Game.getPlayer().getBag().getQuantity(selectedItem));

        // Total display
        totalAmountPanel.drawBackground(g);
        totalAmountPanel.label(g, 18, "Total: " + Global.MONEY_SYMBOL + total);
    }

    public void drawConfirmButton(Graphics g, Button confirmButton, String label) {
        confirmButton.fillTransparent(g);
        if (!confirmButton.isActive()) {
            confirmButton.greyOut(g);
        }

        confirmButton.label(g, 24, label);
        confirmButton.blackOutline(g);
    }
}
