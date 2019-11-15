package mapMaker.tools;

import draw.TileUtils;
import mapMaker.EditType;
import mapMaker.MapMaker;
import util.Point;

import java.awt.Graphics;

// Select tool so you can copy/cut/paste
public class SelectTool extends Tool {
    private boolean paste;
    private boolean selected;

    private int[][] copiedTiles;
    private EditType copiedEditType;
    private Rectangle copiedRectangle;

    private Point startLocation;
    private Rectangle rectangle;

    private boolean pressed = false;

    public SelectTool(MapMaker mapMaker) {
        super(mapMaker, ToolType.SELECT);
        this.rectangle = new Rectangle(true);
        this.copiedRectangle = new Rectangle(false);
    }

    @Override
    public void click(Point clickLocation) {
        if (!paste) {
            return;
        }

        Point location = TileUtils.getLocation(clickLocation, mapMaker.getMapLocation());
        copiedRectangle.setStartLocation(location);
        copiedRectangle.setTiles(mapMaker, copiedTiles);

        this.cancelPaste();
    }

    @Override
    public void released(Point releasedLocation) {
        if (mapMaker.isEditType(EditType.TRIGGERS) || paste || !pressed) {
            return;
        }

        pressed = false;
        select();

        Point mouseHoverLocation = TileUtils.getLocation(releasedLocation, mapMaker.getMapLocation());
        this.rectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.getCurrentMapSize());
    }

    @Override
    public void pressed(Point pressedLocation) {
        if (mapMaker.isEditType(EditType.TRIGGERS) || paste) {
            return;
        }

        this.startLocation = TileUtils.getLocation(pressedLocation, mapMaker.getMapLocation());

        pressed = true;
        deselect();
    }

    @Override
    public void draw(Graphics g) {
        if (!pressed && !selected && !paste) {
            return;
        }

        Point mouseHoverLocation = TileUtils.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());
        if (!selected) {
            this.rectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.getCurrentMapSize());
        }

        if (!paste) {
            this.rectangle.outlineRed(g, mapMaker.getMapLocation());
        } else {
            // Show preview image for all pasting tiles
            copiedRectangle.setStartLocation(mouseHoverLocation);
            copiedRectangle.drawPreview(g, mapMaker, copiedTiles);
            copiedRectangle.outlineRed(g, mapMaker.getMapLocation());
        }
    }

    @Override
    public void reset() {
        pressed = false;
    }

    @Override
    public void cancel() {
        this.cancelPaste();
    }

    // Putting this in its own method in case other logic is added in cancel
    public void cancelPaste() {
        paste = false;
    }

    public boolean hasSelection() {
        return selected;
    }

    public boolean canPaste() {
        return mapMaker.isEditType(copiedEditType) && copiedTiles != null;
    }

    private void select() {
        selected = true;
        mapMaker.setCopyEnabled(true);
    }

    private void deselect() {
        selected = false;
        mapMaker.setCopyEnabled(false);
    }

    public void copy() {
        copiedRectangle.setCoordinates(this.rectangle);
        copiedEditType = mapMaker.getEditType();
        copiedTiles = copiedRectangle.getTiles(mapMaker, copiedEditType.getDataType());

        mapMaker.setPasteEnabled();
    }

    public void cut() {
        copy();

        int val = mapMaker.getModel().getBlankTileIndex();
        this.rectangle.setTiles(mapMaker, val);
    }

    public void paste() {
        paste = true;
        deselect();
    }

    @Override
    public String toString() {
        return "Select";
    }
}
