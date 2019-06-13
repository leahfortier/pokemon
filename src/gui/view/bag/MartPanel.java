package gui.view.bag;

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

    public void drawAmount(Graphics g, int itemAmount) {
        amountPanel.drawBackground(g);
        amountPanel.label(g, 20, itemAmount + "");
    }

    public void drawAmountArrow(Graphics g, Button arrow, Direction arrowDirection) {
        if (!arrow.isActive()) {
            arrow.greyOut(g);
        }

        arrow.fillTransparent(g);
        arrow.blackOutline(g);
        PolygonUtils.drawCenteredArrow(g, arrow.centerX(), arrow.centerY(), 35, 20, arrowDirection);
    }

    public void drawPlayerMoney(Graphics g) {
        playerMoneyPanel.drawBackground(g);
        playerMoneyPanel.label(g, 18, "Money: " + Global.MONEY_SYMBOL + Game.getPlayer().getDatCashMoney());
    }

    public void drawInBagAmount(Graphics g, ItemNamesies selectedItem) {
        inBagPanel.drawBackground(g);
        inBagPanel.label(g, 18, "In Bag: " + Game.getPlayer().getBag().getQuantity(selectedItem));
    }

    public void drawTotalAmount(Graphics g, int total) {
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
