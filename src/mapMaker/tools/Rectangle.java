package mapMaker.tools;

import draw.TileUtils;
import map.MapDataType;
import mapMaker.EditType;
import mapMaker.MapMaker;
import mapMaker.model.MapMakerModel.TileModelType;
import mapMaker.model.TileModel.TileType;
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

    public void setCoordinates(Rectangle other) {
        this.upperLeft = other.upperLeft;
        this.dimension = new Dimension(other.dimension.width, other.dimension.height);
    }

    // Keeps the current dimension but adjusts the start location
    public void setStartLocation(Point startLocation) {
        this.upperLeft = startLocation;
    }

    public void outlineRed(Graphics g, Point mapLocation) {
        TileUtils.outlineTiles(g, this.upperLeft, mapLocation, Color.RED, this.dimension);
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
                int tileVal = tileGetter.getTileVal(x, y);
                Point delta = mapMaker.setTile(Point.add(this.upperLeft, x, y), tileVal);

                // Add the delta in case the map was resized
                this.upperLeft = Point.add(this.upperLeft, delta);
            }
        }
    }

    // Draw all tiles in the rectangle according to the getter (draws over actual map tiles like a preview)
    public void drawPreview(Graphics g, MapMaker mapMaker, int[][] tileVals) {
        for (int x = 0; x < this.dimension.width; x++) {
            for (int y = 0; y < this.dimension.height; y++) {
                Point drawLocation = Point.add(this.upperLeft, x, y);
                int val = tileVals[y][x];

                if (mapMaker.isEditType(EditType.MOVE_MAP) || mapMaker.isEditType(EditType.AREA_MAP)) {
                    TileUtils.fillTile(g, drawLocation, mapMaker.getMapLocation(), new Color(val));
                } else if (mapMaker.getEditType().getModelType() == TileModelType.TILE) {
                    BufferedImage image = mapMaker.getTileFromSet(TileType.MAP, val);
                    if (image != null) {
                        TileUtils.drawTileImage(g, image, drawLocation, mapMaker.getMapLocation());
                    }
                }
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
        int getTileVal(int x, int y);
    }
}
