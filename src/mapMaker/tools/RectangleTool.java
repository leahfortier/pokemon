package mapMaker.tools;

import draw.TileUtils;
import mapMaker.EditType;
import mapMaker.MapMaker;
import pattern.generic.LocationTriggerMatcher;
import util.Point;

import java.awt.Graphics;

class RectangleTool extends Tool {
    private Point startLocation;
    private Rectangle rectangle;

    private Rectangle lastRectangle;
    private int[][] lastValues;
    private EditType lastEditType;
    private LocationTriggerMatcher lastTrigger;

    private boolean pressed = false;

    RectangleTool(MapMaker mapMaker) {
        super(mapMaker);
        this.rectangle = new Rectangle(false);
        this.lastRectangle = new Rectangle(false);
    }

    @Override
    public void released(Point releasedLocation) {
        if (mapMaker.isTileSelectionEmpty() || !pressed) {
            return;
        }

        Tool.lastUsedTool = this;

        Point mouseHoverLocation = TileUtils.getLocation(releasedLocation, mapMaker.getMapLocation());
        this.rectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.getCurrentMapSize());
        this.lastRectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.getCurrentMapSize());

        pressed = false;

        if (!mapMaker.isEditType(EditType.TRIGGERS)) {
            lastValues = this.rectangle.getValues(mapMaker, mapMaker.getEditType().getDataType());
        }

        lastEditType = mapMaker.getEditType();

        int val = mapMaker.getSelectedTile();
        this.rectangle.drawTiles(mapMaker, val);

        if (mapMaker.isEditType(EditType.TRIGGERS)) {
            lastTrigger = mapMaker.getPlaceableTrigger();
            mapMaker.clearPlaceableTrigger();
            mapMaker.setTool(ToolType.TRIGGER);
        }
    }

    @Override
    public void pressed(Point pressedLocation) {
        if (mapMaker.isTileSelectionEmpty()) {
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
                mapMaker.getTriggerData().removeTrigger(lastTrigger);
            } else {
                lastRectangle.drawTiles(mapMaker, lastValues);
            }
            lastValues = null;
            lastEditType = null;
            lastTrigger = null;
        }
    }

    public String toString() {
        return "Rectangle";
    }
}
