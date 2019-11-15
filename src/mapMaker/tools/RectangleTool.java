package mapMaker.tools;

import draw.TileUtils;
import mapMaker.EditType;
import mapMaker.MapMaker;
import pattern.location.LocationTriggerMatcher;
import util.Point;

import java.awt.Graphics;

class RectangleTool extends Tool {
    private Point startLocation;
    private Rectangle rectangle;

    private Rectangle lastRectangle;
    private int[][] lastTiles;
    private EditType lastEditType;
    private LocationTriggerMatcher lastTrigger;
    private boolean[][] lastTriggerRemovals;

    private boolean pressed;

    RectangleTool(MapMaker mapMaker) {
        super(mapMaker, ToolType.RECTANGLE);
        this.rectangle = new Rectangle(false);
        this.lastRectangle = new Rectangle(false);
    }

    @Override
    public void released(Point releasedLocation) {
        if (!mapMaker.hasSelectedTile() || !pressed) {
            return;
        }

        pressed = false;

        mapMaker.setLastUsedTool(this);
        lastEditType = mapMaker.getEditType();

        // Update rectangle coordinates from press location to release location
        Point mouseHoverLocation = TileUtils.getLocation(releasedLocation, mapMaker.getMapLocation());
        rectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.getCurrentMapSize());
        lastRectangle.setCoordinates(this.rectangle);

        // Setup previous values for undo
        if (mapMaker.isEditType(EditType.TRIGGERS)) {
            lastTrigger = mapMaker.getPlaceableTrigger();
            lastTriggerRemovals = null;
        } else {
            lastTiles = this.rectangle.getTiles(mapMaker, lastEditType.getDataType());
        }

        // Set every tile in the current rectangle to the current tile or trigger
        // Unless removing, in which case remove all current trigger instances in the rectangle
        if (mapMaker.isRemovingTriggers()) {
            // Store exactly which locations were removed (for undo purposes)
            lastTriggerRemovals = this.rectangle.removeTriggers(mapMaker, mapMaker.getPlaceableTrigger());
        } else {
            rectangle.setTiles(mapMaker, mapMaker.getSelectedTile());
        }

        // If setting a trigger, clear the current trigger since we just set it up
        // Unless we're doing some partial removals -- then it makes sense to just keep doing that
        if (mapMaker.isEditType(EditType.TRIGGERS) && !mapMaker.isRemovingTriggers()) {
            mapMaker.clearPlaceableTrigger();
            mapMaker.setTool(ToolType.TRIGGER);
        }
    }

    @Override
    public void pressed(Point pressedLocation) {
        if (!mapMaker.hasSelectedTile()) {
            return;
        }

        pressed = true;
        startLocation = TileUtils.getLocation(pressedLocation, mapMaker.getMapLocation());
    }

    @Override
    public void draw(Graphics g) {
        Point mouseHoverLocation = TileUtils.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());

        if (!pressed) {
            TileUtils.outlineTileRed(g, mouseHoverLocation, mapMaker.getMapLocation());
        } else {
            this.rectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.getCurrentMapSize());
            this.rectangle.outlineRed(g, mapMaker.getMapLocation());
        }
    }

    @Override
    public void reset() {
        pressed = false;
    }

    @Override
    public void undo() {
        if (lastRectangle != null && lastEditType != null) {
            if (lastEditType == EditType.TRIGGERS) {
                // If the last action was actually a removal, then add them back!
                if (lastTriggerRemovals != null) {
                    lastRectangle.placeTriggers(mapMaker, lastTriggerRemovals, lastTrigger);
                } else {
                    lastRectangle.removeTriggers(mapMaker, lastTrigger);
                }
            } else {
                lastRectangle.setTiles(mapMaker, lastTiles);
            }

            lastTiles = null;
            lastEditType = null;
            lastTrigger = null;
        }
    }

    @Override
    public String toString() {
        return "Rectangle";
    }
}
