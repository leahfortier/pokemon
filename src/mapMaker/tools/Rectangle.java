package mapMaker.tools;

import util.DrawUtils;
import util.Point;
import mapMaker.MapMaker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Rectangle {
    private boolean inBoundsRequired;

    private Point upperLeftRectangleCoordinate;
    private Dimension dimension;

    public Rectangle(boolean inBoundsRequired) {
        this.inBoundsRequired = inBoundsRequired;
    }

    void setCoordinates(Point startLocation, Point endLocation, Dimension dimension) {

        this.upperLeftRectangleCoordinate = Point.min(startLocation, endLocation);
        Point lowerRightRectangleCoordinate = Point.max(startLocation, endLocation);

        if (this.inBoundsRequired) {
            this.upperLeftRectangleCoordinate.lowerBound();
            lowerRightRectangleCoordinate.upperBound(dimension);
        }

        this.dimension = new Dimension(
                lowerRightRectangleCoordinate.x - this.upperLeftRectangleCoordinate.x + 1,
                lowerRightRectangleCoordinate.y - this.upperLeftRectangleCoordinate.y + 1
        );

        System.out.println("New rectangle with dimension " + this.dimension.width + " " + this.dimension.height);
    }

    void outlineRed(Graphics g, Point mapLocation) {
        DrawUtils.outlineTiles(g, this.upperLeftRectangleCoordinate, mapLocation, Color.RED, this.dimension);
    }

    BufferedImage getImage(BufferedImage currentImage) {
        int width = this.dimension.width;
        int height = this.dimension.height;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[] rgb = currentImage.getRGB(upperLeftRectangleCoordinate.x, upperLeftRectangleCoordinate.y, width, height, null, 0, width);
        image.setRGB(0, 0, width, height, rgb, 0, width);

        return image;
    }

    void drawTiles(MapMaker mapMaker, int val) {
        for (int x = 0; x < this.dimension.width; x++) {
            for (int y = 0; y < this.dimension.height; y++) {
                Point delta = mapMaker.setTile(new Point(x, y).add(this.upperLeftRectangleCoordinate), val);
                this.upperLeftRectangleCoordinate.add(delta);
            }
        }
    }
}
