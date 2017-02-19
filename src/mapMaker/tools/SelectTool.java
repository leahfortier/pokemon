package mapMaker.tools;

import draw.TileUtils;
import mapMaker.EditType;
import mapMaker.MapMaker;
import mapMaker.model.TileModel.TileType;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

// Select tool so you can copy/cut/paste
public class SelectTool extends Tool {
    private boolean paste;
    private boolean selected;
    private boolean controlClick;

    private BufferedImage copiedTiles = null;
    private EditType copiedEditType;

    private Point startLocation;
    private Rectangle rectangle;

    private boolean pressed = false;

    public SelectTool(MapMaker mapMaker) {
        super(mapMaker);
        this.rectangle = new Rectangle(true);
    }

    @Override
    public void click(Point clickLocation) {
        if (!paste || controlClick) {
            return;
        }

        Point location = TileUtils.getLocation(clickLocation, mapMaker.getMapLocation());
        for (int currX = 0; currX < copiedTiles.getWidth(); currX++) {
            for (int currY = 0; currY < copiedTiles.getHeight(); currY++) {
                int val = copiedTiles.getRGB(currX, currY);
                Point delta = mapMaker.setTile(Point.add(location, currX, currY), val);
                location = Point.add(location, delta);
            }
        }

        paste = false;
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

//			if (controlKeyDown && selected) {
//				cut();
//				controlClick = true;
//				return;
//			}

        this.startLocation = TileUtils.getLocation(pressedLocation, mapMaker.getMapLocation());

        pressed = true;
        deselect();
    }

    @Override
    public void drag(Point dragLocation) {
//			if (controlClick) {
//				controlClick = false;
//				paste();
//				//return;
//			}
//
//			click(x, y);
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
            // Show preview image for all pasting tiles.
            for (int currX = 0; currX < copiedTiles.getWidth(); currX++) {
                for (int currY = 0; currY < copiedTiles.getHeight(); currY++) {

                    int val = copiedTiles.getRGB(currX, currY);
                    Point previewLocation = new Point(mouseHoverLocation.x + currX, mouseHoverLocation.y + currY);

                    if (mapMaker.isEditType(EditType.BACKGROUND) || mapMaker.isEditType(EditType.FOREGROUND)) {
                        BufferedImage image = mapMaker.getTileFromSet(TileType.MAP, val);
                        if (image != null) {
                            TileUtils.drawTileImage(g, image, previewLocation, mapMaker.getMapLocation());
                        }
                    } else if (mapMaker.isEditType(EditType.MOVE_MAP) || mapMaker.isEditType(EditType.AREA_MAP)) {
                        TileUtils.outlineTile(g, previewLocation, mapMaker.getMapLocation(), new Color(val));
                    }
                }
            }

            TileUtils.outlineTileRed(g, mouseHoverLocation, mapMaker.getMapLocation());
        }
    }

    @Override
    public void reset() {
        pressed = false;
    }

    public boolean hasSelection() {
        return selected;
    }

    public boolean canPaste() {
        return mapMaker.isEditType(copiedEditType) && copiedTiles != null;
    }

    public void select() {
        selected = true;
        mapMaker.copyMenuItem.setEnabled(true);
        mapMaker.cutMenuItem.setEnabled(true);
    }

    public void deselect() {
        selected = false;
        mapMaker.copyMenuItem.setEnabled(false);
        mapMaker.cutMenuItem.setEnabled(false);
    }

    public void copy() {
        copiedEditType = mapMaker.getEditType();
        BufferedImage currentMapImage = mapMaker.getCurrentMapImage(copiedEditType.getDataType());
        copiedTiles = this.rectangle.getImage(currentMapImage);

        if (!mapMaker.pasteMenuItem.isEnabled()) {
            mapMaker.pasteMenuItem.setEnabled(true);
        }
    }

    public void cut() {
        copy();

        int val = mapMaker.getModel().getBlankTileIndex();
        this.rectangle.drawTiles(mapMaker, val);
    }

    public void paste() {
        paste = true;
        deselect();
    }

    @Override
    public void undo() {

    }

    public String toString() {
        return "Select";
    }
}
