package mapMaker.tools;

import mapMaker.MapMaker;
import mapMaker.MapMaker.EditType;
import mapMaker.TileMap.TileType;
import util.DrawMetrics;
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

        mapMaker.saved = false;

        Point location = DrawMetrics.getLocation(clickLocation, mapMaker.getMapLocation());
        for (int currX = 0; currX < copiedTiles.getWidth(); currX++) {
            for (int currY = 0; currY < copiedTiles.getHeight(); currY++) {
                int val = copiedTiles.getRGB(currX, currY);
                Point delta = mapMaker.setTile(new Point(currX, currY).add(location), val);
                location.add(delta);
            }
        }

        paste = false;
    }

    @Override
    public void released(Point releasedLocation) {
        if (mapMaker.editType == EditType.TRIGGERS || paste || !pressed) {
            return;
        }

        pressed = false;
        select();

        Point mouseHoverLocation = DrawMetrics.getLocation(releasedLocation, mapMaker.getMapLocation());
        this.rectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.currentMapSize);
    }

    @Override
    public void pressed(Point pressedLocation) {
        if (mapMaker.editType == EditType.TRIGGERS || paste) {
            return;
        }

//			if (controlKeyDown && selected) {
//				cut();
//				controlClick = true;
//				return;
//			}

        this.startLocation = DrawMetrics.getLocation(pressedLocation, mapMaker.getMapLocation());

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

        Point mouseHoverLocation = DrawMetrics.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());

        if (!selected) {
            this.rectangle.setCoordinates(startLocation, mouseHoverLocation, mapMaker.currentMapSize);
        }

        if (!paste) {
            this.rectangle.outlineRed(g, mapMaker.getMapLocation());
        } else {
            // Show preview image for all pasting tiles.
            for (int currX = 0; currX < copiedTiles.getWidth(); currX++) {
                for (int currY = 0; currY < copiedTiles.getHeight(); currY++) {

                    int val = copiedTiles.getRGB(currX, currY);
                    Point previewLocation = new Point(mouseHoverLocation.x + currX, mouseHoverLocation.y + currY);

                    if (mapMaker.editType == EditType.BACKGROUND || mapMaker.editType == EditType.FOREGROUND) {
                        BufferedImage image = mapMaker.getTileFromSet(TileType.MAP, val);
                        if (image != null) {
                            DrawMetrics.drawTileImage(g, image, previewLocation, mapMaker.getMapLocation());
                        }
                    } else if (mapMaker.editType == EditType.MOVE_MAP || mapMaker.editType == EditType.AREA_MAP) {
                        DrawMetrics.outlineTile(g, previewLocation, mapMaker.getMapLocation(), new Color(val));
                    }
                }
            }

            DrawMetrics.outlineTileRed(g, mouseHoverLocation, mapMaker.getMapLocation());
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
        return copiedEditType == mapMaker.editType && copiedTiles != null;
    }

    public void select() {
        selected = true;
        mapMaker.mntmCopy.setEnabled(true);
        mapMaker.mntmCut.setEnabled(true);
    }

    public void deselect() {
        selected = false;
        mapMaker.mntmCopy.setEnabled(false);
        mapMaker.mntmCut.setEnabled(false);
    }

    public void copy() {
        copiedEditType = mapMaker.editType;

        BufferedImage currentMapImage = null;
        switch (mapMaker.editType) {
            case FOREGROUND:
                currentMapImage = mapMaker.currentMapFg;
                break;
            case BACKGROUND:
                currentMapImage = mapMaker.currentMapBg;
                break;
            case MOVE_MAP:
                currentMapImage = mapMaker.currentMapMove;
                break;
            case AREA_MAP:
                currentMapImage = mapMaker.currentMapArea;
                break;
        }

        copiedTiles = this.rectangle.getImage(currentMapImage);

        if (!mapMaker.mntmPaste.isEnabled()) {
            mapMaker.mntmPaste.setEnabled(true);
        }
    }

    public void cut() {
        copy();

        int val = Integer.parseInt(mapMaker.tileList.getModel().getElementAt(0).getDescription());
        this.rectangle.drawTiles(mapMaker, val);
    }

    public void paste() {
        paste = true;
        deselect();
    }

    public String toString() {
        return "Select";
    }
}
