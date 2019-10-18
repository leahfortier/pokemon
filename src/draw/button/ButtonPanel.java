package draw.button;

import draw.Alignment;
import draw.ImageUtils;
import draw.PolygonUtils;
import draw.TextUtils;
import draw.panel.DrawPanel;
import gui.TileSet;
import item.Item;
import item.ItemNamesies;
import item.bag.Bag;
import main.Game;
import map.Direction;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ButtonPanel extends DrawPanel {
    private final Button button;

    private Direction arrowDirection;

    private boolean onlyActiveDraw;
    private boolean greyInactive;

    private ItemNamesies item;
    private boolean includeQuantity;

    // Should only be created from Button constructor
    ButtonPanel(Button button) {
        super(button);

        this.button = button;

        // By default, button has a black outline and no background or border
        this.withBlackOutline();
        this.withBackgroundColor(null);
        this.withBorderPercentage(0);
    }

    public Button button() {
        return this.button;
    }

    public ButtonPanel onlyActiveDraw() {
        this.onlyActiveDraw = true;
        return this;
    }

    public ButtonPanel greyInactive() {
        this.greyInactive = true;
        return this;
    }

    public void setItem(ItemNamesies item, boolean includeQuantity) {
        this.item = item;
        this.includeQuantity = includeQuantity;
    }

    // Sets the arrow direction and removes the black outline
    public ButtonPanel asArrow(Direction arrowDirection) {
        this.arrowDirection = arrowDirection;
        return this.withNoOutline().asButtonPanel();
    }

    @Override
    public void draw(Graphics g) {
        if (!button.isActive()) {
            if (this.onlyActiveDraw) {
                // If only draw when active, and is not active, skip the current draw
                super.skipDraw();
            } else if (this.greyInactive) {
                // If button is inactive, set greyOut to true
                // Note: This is not actually drawing the grey out, just setting it (will be drawn in drawBackground)
                super.setGreyOut();
            }
        }

        super.draw(g);

        // Arrow buttons!
        if (this.arrowDirection != null) {
            PolygonUtils.drawArrow(g, x, y, width, height, arrowDirection);
        }

        // Item name, image, and (maybe) quantity
        if (item != null) {
            drawItem(g);
        }
    }

    private void drawItem(Graphics g) {
        TileSet itemTiles = Game.getData().getItemTiles();
        Bag bag = Game.getPlayer().getBag();
        Item itemValue = item.getItem();

        int spacing = this.getTextSpace(g);
        int startX = x + spacing + Item.MAX_IMAGE_SIZE.width;
        int centerY = this.centerY();

        BufferedImage itemImage = itemTiles.getTile(itemValue.getImageName());
        ImageUtils.drawCenteredImage(g, itemImage, x + (startX - x)/2, centerY);

        TextUtils.drawCenteredHeightString(g, item.getName(), startX, centerY);

        if (includeQuantity && itemValue.hasQuantity()) {
            int rightX = this.rightX() - spacing;
            TextUtils.drawCenteredHeightString(g, "x" + bag.getQuantity(item), rightX, centerY, Alignment.RIGHT);
        }
    }
}
