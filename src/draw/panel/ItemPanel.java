package draw.panel;

import draw.ImageUtils;
import draw.TextUtils;
import draw.panel.WrapPanel.WrapMetrics;
import gui.TileSet;
import item.Item;
import item.ItemNamesies;
import main.Game;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ItemPanel extends DrawPanel {
    private final boolean includeQuantity;

    public ItemPanel(int x, int y, int width, int height, boolean includeQuantity) {
        super(x, y, width, height);

        this.includeQuantity = includeQuantity;

        this.withFullTransparency();
        this.withBlackOutline();
    }

    // Draws all relevant information about the item
    // If item is NO_ITEM, will only draw background and will return null
    public WrapMetrics drawMessage(Graphics g, ItemNamesies itemNamesies) {
        // Only draw actual items
        if (itemNamesies == ItemNamesies.NO_ITEM) {
            return null;
        }

        int spacing = 8;

        TileSet itemTiles = Game.getData().getItemTiles();
        Item item = itemNamesies.getItem();

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 20);

        // Tile size for item image tile, double spacing for original and after image
        int nameX = x + 2*spacing + Item.MAX_IMAGE_SIZE.width;
        int startY = y + FontMetrics.getDistanceBetweenRows(g);

        // Draw item image
        BufferedImage img = itemTiles.getTile(item.getImageName());
        ImageUtils.drawBottomCenteredImage(g, img, x + (nameX - x)/2, startY);

        // Draw item name to the right of the image
        g.drawString(item.getName(), nameX, startY);

        // Draw quantity if applicable
        if (includeQuantity && item.hasQuantity()) {
            String quantityString = "x" + Game.getPlayer().getBag().getQuantity(itemNamesies);
            TextUtils.drawRightAlignedString(g, quantityString, this.rightX() - 2*spacing, startY);
        }

        // Draw the description underneath everything else as wrapped text
        WrapPanel descriptionPanel = new WrapPanel(
                this.x,
                startY,
                this.width,
                this.bottomY() - startY,
                14
        )
                .withBorderPercentage(0)
                .withMinimumSpacing(2)
                .withStartX(x + spacing)
                .withMinFontSize(11, true);
        return descriptionPanel.drawMessage(g, item.getDescription());
    }
}
