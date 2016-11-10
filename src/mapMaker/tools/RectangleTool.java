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

    public void released(final int x, final int y) {
        if (mapMaker.tileList.isSelectionEmpty() || !pressed) {
            return;
        }

        Point mouseHoverLocation = DrawMetrics.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());
        this.rectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.currentMapSize);

        pressed = false;

        int val = Integer.parseInt(mapMaker.tileList.getSelectedValue().getDescription());
        this.rectangle.drawTiles(mapMaker, val);

        if (mapMaker.editType == EditType.TRIGGERS) {
            mapMaker.triggerData.clearPlaceableTrigger();
            mapMaker.toolList.setSelectedIndex(3); // TODO
        }
    }

    public void pressed(int x, int y) {
        if (mapMaker.tileList.isSelectionEmpty()) {
            return;
        }

        pressed = true;
        startLocation = DrawMetrics.getLocation(new Point(x, y), mapMaker.getMapLocation());
    }

    public void drag(int x, int y) {
    }

    public void draw(Graphics g) {
        Point mouseHoverLocation = DrawMetrics.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());

        if (!pressed) {
            DrawMetrics.outlineTileRed(g, mouseHoverLocation, mapMaker.getMapLocation());
        } else {
            this.rectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.currentMapSize);
            this.rectangle.outlineRed(g, mapMaker.getMapLocation());
        }
    }

    public String toString() {
        return "Rectangle";
    }

    public void reset() {
        pressed = false;
    }
}
