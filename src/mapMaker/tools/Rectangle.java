package mapMaker.tools;

import draw.TileUtils;
import map.MapDataType;
import mapMaker.MapMaker;
import pattern.location.LocationTriggerMatcher;
import util.Point;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

class Rectangle {
    private final boolean inBoundsRequired;

    private Point upperLeft;
    private Dimension dimension;

    public Rectangle(boolean inBoundsRequired) {
        this.inBoundsRequired = inBoundsRequired;
    }

    public void setCoordinates(Point startLocation, Point endLocation, Dimension dimension) {
        this.upperLeft = Point.min(startLocation, endLocation);
        Point lowerRight = Point.max(startLocation, endLocation);

        if (this.inBoundsRequired) {
            this.upperLeft = Point.lowerBound(this.upperLeft);
            lowerRight = Point.upperBound(lowerRight, dimension);
        }

        this.dimension = new Dimension(
                lowerRight.x - this.upperLeft.x + 1,
                lowerRight.y - this.upperLeft.y + 1
        );
    }

    public void outlineRed(Graphics g, Point mapLocation) {
        TileUtils.outlineTiles(g, this.upperLeft, mapLocation, Color.RED, this.dimension);
    }

    public BufferedImage getImage(BufferedImage currentImage) {
        int width = this.dimension.width;
        int height = this.dimension.height;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[] rgb = currentImage.getRGB(upperLeft.x, upperLeft.y, width, height, null, 0, width);
        image.setRGB(0, 0, width, height, rgb, 0, width);

        return image;
    }

    // Set every tile in the rectangle to val
    public void setTiles(MapMaker mapMaker, int val) {
        this.setTiles(mapMaker, (x, y) -> val);
    }

    // Set each tile to the corresponding value
    public void setTiles(MapMaker mapMaker, int[][] values) {
        this.setTiles(mapMaker, (x, y) -> values[y][x]);
    }

    // Set all tiles in the rectangle according to the getter
    private void setTiles(MapMaker mapMaker, TileGetter tileGetter) {
        for (int x = 0; x < this.dimension.width; x++) {
            for (int y = 0; y < this.dimension.height; y++) {
                int val = tileGetter.getValue(x, y);
                Point delta = mapMaker.setTile(Point.add(this.upperLeft, x, y), val);
                this.upperLeft = Point.add(this.upperLeft, delta);
            }
        }
    }

    // Returns the current tile values inside the rectangle
    public int[][] getTiles(MapMaker mapMaker, MapDataType mapDataType) {
        int[][] values = new int[dimension.height][dimension.width];
        for (int x = 0; x < this.dimension.width; x++) {
            for (int y = 0; y < this.dimension.height; y++) {
                values[y][x] = mapMaker.getTile(Point.add(this.upperLeft, x, y), mapDataType);
            }
        }

        return values;
    }

    // Removes the specified trigger at all coordinates of the rectangle
    public boolean[][] removeTriggers(MapMaker mapMaker, LocationTriggerMatcher toRemove) {
        boolean[][] removed = new boolean[dimension.height][dimension.width];
        for (int x = 0; x < this.dimension.width; x++) {
            for (int y = 0; y < this.dimension.height; y++) {
                removed[y][x] = mapMaker.getTriggerData().removeTriggerAtPoint(toRemove, Point.add(this.upperLeft, x, y));
            }
        }
        return removed;
    }

    // Removes the specified trigger at all coordinates of the rectangle
    public void placeTriggers(MapMaker mapMaker, boolean[][] shouldAdd, LocationTriggerMatcher toRemove) {
        for (int x = 0; x < this.dimension.width; x++) {
            for (int y = 0; y < this.dimension.height; y++) {
                if (shouldAdd[y][x]) {
                    mapMaker.getTriggerData().placeTrigger(toRemove, Point.add(this.upperLeft, x, y));
                }
            }
        }
    }

    // Retrieves a tile value from the rectangle coordinate
    @FunctionalInterface
    private interface TileGetter {
        int getValue(int x, int y);
    }
}
