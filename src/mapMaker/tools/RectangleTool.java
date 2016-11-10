package mapMaker.tools;

import util.Point;
import mapMaker.MapMaker;
import mapMaker.MapMaker.EditType;
import util.DrawMetrics;

import java.awt.Graphics;

public class RectangleTool extends Tool {
    private Point startLocation;
    private Rectangle rectangle;

    private boolean pressed = false;

    public RectangleTool(MapMaker mapMaker) {
        super(mapMaker);
        this.rectangle = new Rectangle(false);
    }

    @Override
    public void released(Point releasedLocation) {
        if (mapMaker.tileList.isSelectionEmpty() || !pressed) {
            return;
        }

        Point mouseHoverLocation = DrawMetrics.getLocation(releasedLocation, mapMaker.getMapLocation());
        this.rectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.currentMapSize);

        pressed = false;

        int val = Integer.parseInt(mapMaker.tileList.getSelectedValue().getDescription());
        this.rectangle.drawTiles(mapMaker, val);

        if (mapMaker.editType == EditType.TRIGGERS) {
            mapMaker.triggerData.clearPlaceableTrigger();
            mapMaker.toolList.setSelectedIndex(3); // TODO
        }
    }

    @Override
    public void pressed(Point pressedLocation) {
        if (mapMaker.tileList.isSelectionEmpty()) {
            return;
        }

        pressed = true;
        startLocation = DrawMetrics.getLocation(pressedLocation, mapMaker.getMapLocation());
    }

    @Override
    public void draw(Graphics g) {
        Point mouseHoverLocation = DrawMetrics.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());

        if (!pressed) {
            DrawMetrics.outlineTileRed(g, mouseHoverLocation, mapMaker.getMapLocation());
        } else {
            this.rectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.currentMapSize);
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
