package draw.panel;

import draw.Alignment;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.ButtonPanel;
import gui.TileSet;
import item.Item;
import item.ItemNamesies;
import main.Game;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ItemButtonPanel {
    private final ButtonPanel parent;
    private final boolean includeQuantity;

    private ItemNamesies item;

    public ItemButtonPanel(ButtonPanel parent, boolean includeQuantity) {
        this.parent = parent;
        this.includeQuantity = includeQuantity;

        this.parent.withBackgroundColor(Color.WHITE)
                   .withBlackOutline()
                   .withBorderPercentage(0);
    }

    public void setItem(ItemNamesies item) {
        this.item = item;
    }

    public void drawItem(Graphics g) {
        TileSet itemTiles = Game.getData().getItemTiles();

        Item itemValue = item.getItem();

        FontMetrics.setBlackFont(g, 12);
        int spacing = parent.getSpace(g);
        int startX = parent.x + spacing + Item.MAX_IMAGE_SIZE.width;
        int centerY = parent.centerY();

        BufferedImage itemImage = itemTiles.getTile(itemValue.getImageName());
        ImageUtils.drawCenteredImage(g, itemImage, (parent.x + startX)/2, centerY);
        TextUtils.drawCenteredHeightString(g, item.getName(), startX, centerY);

        if (includeQuantity && itemValue.hasQuantity()) {
            int rightX = parent.rightX() - spacing;
            int quantity = Game.getPlayer().getBag().getQuantity(item);
            TextUtils.drawCenteredHeightString(g, "x" + quantity, rightX, centerY, Alignment.RIGHT);
        }
    }
}
