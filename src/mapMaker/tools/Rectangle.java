package mapMaker.tools;

import util.Point;
import mapMaker.MapMaker;
import util.DrawMetrics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Rectangle {
    private boolean inBoundsRequired;

    private Point upperLeftRectangleCoordinate;
    private Point lowerRightRectangleCoordinate;
    private Dimension dimension;

    public Rectangle(boolean inBoundsRequired) {
        this.inBoundsRequired = inBoundsRequired;
    }

    public void setCoordinates(Point startLocation, Point endLocation, Dimension dimension) {

        this.upperLeftRectangleCoordinate = Point.min(startLocation, endLocation);
        this.lowerRightRectangleCoordinate = Point.max(startLocation, endLocation);

        if (this.inBoundsRequired) {
            this.upperLeftRectangleCoordinate.lowerBound();
            this.lowerRightRectangleCoordinate.upperBound(dimension);
        }

        this.dimension = new Dimension(
                this.lowerRightRectangleCoordinate.x - this.upperLeftRectangleCoordinate.x + 1,
                this.lowerRightRectangleCoordinate.y - this.upperLeftRectangleCoordinate.y + 1
        );
    }

    public void outlineRed(Graphics g, Point mapLocation) {
        DrawMetrics.outlineTiles(g, this.upperLeftRectangleCoordinate, mapLocation, Color.RED, this.dimension);
    }

    public BufferedImage getImage(BufferedImage currentImage) {
        int width = this.dimension.width;
        int height = this.dimension.height;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[] rgb = currentImage.getRGB(upperLeftRectangleCoordinate.x, upperLeftRectangleCoordinate.y, width, height, null, 0, width);
        image.setRGB(0, 0, width, height, rgb, 0, width);

        return image;
    }

    public void drawTiles(MapMaker mapMaker, int val) {
        for (int x = 0; x < this.dimension.width; x++) {
            for (int y = 0; y < this.dimension.height; y++) {
                Point delta = mapMaker.setTile(new Point(x, y).add(this.upperLeftRectangleCoordinate), val);
                this.upperLeftRectangleCoordinate.add(delta);
            }
        }
    }
}
