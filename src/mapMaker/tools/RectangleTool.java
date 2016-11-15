package mapMaker.tools;

import mapMaker.MapMaker;
import mapMaker.EditType;
import util.DrawUtils;
import util.Point;

import java.awt.Graphics;

class RectangleTool extends Tool {
    private Point startLocation;
    private Rectangle rectangle;

    private boolean pressed = false;

    RectangleTool(MapMaker mapMaker) {
        super(mapMaker);
        this.rectangle = new Rectangle(false);
    }

    @Override
    public void released(Point releasedLocation) {
        if (mapMaker.isTileSelectionEmpty() || !pressed) {
            return;
        }

        Point mouseHoverLocation = DrawUtils.getLocation(releasedLocation, mapMaker.getMapLocation());
        this.rectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.getCurrentMapSize());

        pressed = false;

        int val = mapMaker.getSelectedTile();
        this.rectangle.drawTiles(mapMaker, val);

        if (mapMaker.isEditType(EditType.TRIGGERS)) {
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
        startLocation = DrawUtils.getLocation(pressedLocation, mapMaker.getMapLocation());
    }

    @Override
    public void draw(Graphics g) {
        Point mouseHoverLocation = DrawUtils.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());

        if (!pressed) {
            DrawUtils.outlineTileRed(g, mouseHoverLocation, mapMaker.getMapLocation());
        } else {
            this.rectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.getCurrentMapSize());
            this.rectangle.outlineRed(g, mapMaker.getMapLocation());
        }
    }

    @Override
    public void reset() {
        pressed = false;
    }

    public String toString() {
        return "Rectangle";
    }
}
