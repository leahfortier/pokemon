package mapMaker.tools;

import draw.TileUtils;
import map.MapDataType;
import mapMaker.MapMaker;
import util.Point;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

class Rectangle {
    private final boolean inBoundsRequired;

    private Point upperLeftRectangleCoordinate;
    private Dimension dimension;

    public Rectangle(boolean inBoundsRequired) {
        this.inBoundsRequired = inBoundsRequired;
    }

    void setCoordinates(Point startLocation, Point endLocation, Dimension dimension) {

        this.upperLeftRectangleCoordinate = Point.min(startLocation, endLocation);
        Point lowerRightRectangleCoordinate = Point.max(startLocation, endLocation);

        if (this.inBoundsRequired) {
            this.upperLeftRectangleCoordinate = Point.lowerBound(this.upperLeftRectangleCoordinate);
            lowerRightRectangleCoordinate = Point.upperBound(lowerRightRectangleCoordinate, dimension);
        }

        this.dimension = new Dimension(
                lowerRightRectangleCoordinate.x - this.upperLeftRectangleCoordinate.x + 1,
                lowerRightRectangleCoordinate.y - this.upperLeftRectangleCoordinate.y + 1
        );
    }

    void outlineRed(Graphics g, Point mapLocation) {
        TileUtils.outlineTiles(g, this.upperLeftRectangleCoordinate, mapLocation, Color.RED, this.dimension);
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
                Point delta = mapMaker.setTile(Point.add(this.upperLeftRectangleCoordinate, x, y), val);
                this.upperLeftRectangleCoordinate = Point.add(this.upperLeftRectangleCoordinate, delta);
            }
        }
    }

    void drawTiles(MapMaker mapMaker, int[][] values) {
        for (int x = 0; x < this.dimension.width; x++) {
            for (int y = 0; y < this.dimension.height; y++) {
                Point delta = mapMaker.setTile(Point.add(this.upperLeftRectangleCoordinate, x, y), values[y][x]);
                this.upperLeftRectangleCoordinate = Point.add(this.upperLeftRectangleCoordinate, delta);
            }
        }
    }

    int[][] getValues(MapMaker mapMaker, MapDataType mapDataType) {
        int[][] values = new int[dimension.height][dimension.width];
        for (int x = 0; x < this.dimension.width; x++) {
            for (int y = 0; y < this.dimension.height; y++) {
                values[y][x] = mapMaker.getTile(Point.add(this.upperLeftRectangleCoordinate, x, y), mapDataType);
            }
        }

        return values;
    }
}
