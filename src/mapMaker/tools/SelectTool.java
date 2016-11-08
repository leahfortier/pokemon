package mapMaker.tools;

import mapMaker.MapMaker;
import mapMaker.EditType;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

// Select tool so you can copy/cut/paste
public class SelectTool extends Tool {
    private boolean paste;
    private boolean selected;
    private boolean controlClick;

    private BufferedImage copiedTiles = null;
    private EditType copiedEditType;

    private int startX, startY;
    private int tx, ty, bx, by;

    private boolean pressed = false;

    public SelectTool(MapMaker mapMaker) {
        super(mapMaker);
    }

    public void click(int x, int y) {
        if (!paste || controlClick) {
            return;
        }

        mapMaker.saved = false;

        x = (int) Math.floor((x - mapMaker.mapX) * 1.0 / MapMaker.tileSize);
        y = (int) Math.floor((y - mapMaker.mapY) * 1.0 / MapMaker.tileSize);

        for (int currX = 0; currX < copiedTiles.getWidth(); ++currX) {
            for (int currY = 0; currY < copiedTiles.getHeight(); ++currY) {
                int val = copiedTiles.getRGB(currX, currY);
                Point delta = mapMaker.setTile(x + currX, y + currY, val);

                if (delta.x != 0) {
                    x += delta.x;
                }

                if (delta.y != 0) {
                    y += delta.y;
                }
            }
        }

        paste = false;
    }

    public void released(int x, int y) {
        if (mapMaker.editType == EditType.TRIGGERS || paste || !pressed) {
            return;
        }

        pressed = false;
        select();

        int mhx = (int) Math.floor((mapMaker.mouseHoverX - mapMaker.mapX) * 1.0 / MapMaker.tileSize);
        int mhy = (int) Math.floor((mapMaker.mouseHoverY - mapMaker.mapY) * 1.0 / MapMaker.tileSize);

        tx = Math.max(Math.min(startX, mhx), 0);
        ty = Math.max(Math.min(startY, mhy), 0);
        bx = Math.min(Math.max(startX, mhx), mapMaker.currentMapSize.width - 1);
        by = Math.min(Math.max(startY, mhy), mapMaker.currentMapSize.height - 1);

        if (tx > bx || ty > by) {
            deselect();
        }
    }

    public void pressed(int x, int y) {
        if (mapMaker.editType == EditType.TRIGGERS || paste) {
            return;
        }

//			if (controlKeyDown && selected) {
//				cut();
//				controlClick = true;
//				return;
//			}

        startX = (int) Math.floor((x - mapMaker.mapX) * 1.0 / MapMaker.tileSize);
        startY = (int) Math.floor((y - mapMaker.mapY) * 1.0 / MapMaker.tileSize);

        pressed = true;
        deselect();
    }

    public void drag(int x, int y) {
//			if (controlClick) {
//				controlClick = false;
//				paste();
//				//return;
//			}
//
//			click(x, y);
    }

    public void draw(Graphics g) {
        if (!pressed && !selected && !paste) {
            return;
        }

        int mhx = (int) Math.floor((mapMaker.mouseHoverX - mapMaker.mapX) * 1.0 / MapMaker.tileSize);
        int mhy = (int) Math.floor((mapMaker.mouseHoverY - mapMaker.mapY) * 1.0 / MapMaker.tileSize);

        //int tx, ty, bx, by;
        if (!selected) {
            tx = Math.max(Math.min(startX, mhx), 0);
            ty = Math.max(Math.min(startY, mhy), 0);
            bx = Math.min(Math.max(startX, mhx), mapMaker.currentMapSize.width - 1);
            by = Math.min(Math.max(startY, mhy), mapMaker.currentMapSize.height - 1);
        }

        if (!paste) {
            g.setColor(Color.RED);
            g.drawRect(tx * MapMaker.tileSize + mapMaker.mapX, ty * MapMaker.tileSize + mapMaker.mapY, MapMaker.tileSize * (bx - tx + 1), MapMaker.tileSize * (by - ty + 1));
        } else {
            // Show preview image for all pasting tiles.
            for (int currX = 0; currX < copiedTiles.getWidth(); ++currX) {
                for (int currY = 0; currY < copiedTiles.getHeight(); ++currY) {
                    int val = copiedTiles.getRGB(currX, currY);
                    if (mapMaker.editType == EditType.BACKGROUND || mapMaker.editType == EditType.FOREGROUND) {
                        if (!mapMaker.tileMap.containsKey(val)) {
                            continue;
                        }

                        BufferedImage img = mapMaker.tileMap.get(val);
                        g.drawImage(mapMaker.tileMap.get(val), (mhx + currX) * MapMaker.tileSize + mapMaker.mapX - img.getWidth() + MapMaker.tileSize, (mhy + currY) * MapMaker.tileSize + mapMaker.mapY - img.getHeight() + MapMaker.tileSize, null);
                    } else if (mapMaker.editType == EditType.MOVE_MAP || mapMaker.editType == EditType.AREA_MAP) {
                        g.setColor(new Color(val));
                        g.fillRect((mhx + currX) * MapMaker.tileSize + mapMaker.mapX, (mhy + currY) * MapMaker.tileSize + mapMaker.mapY, MapMaker.tileSize, MapMaker.tileSize);
                    }
                }
            }

            g.setColor(Color.red);
            g.drawRect(mhx * MapMaker.tileSize + mapMaker.mapX, mhy * MapMaker.tileSize + mapMaker.mapY, MapMaker.tileSize * copiedTiles.getWidth(), MapMaker.tileSize * copiedTiles.getHeight());
        }
    }

    public void reset() {
        pressed = false;
    }

    public String toString() {
        return "Select";
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
        if (mapMaker.editType == EditType.FOREGROUND) {
            currentMapImage = mapMaker.currentMapFg;
        } else if (mapMaker.editType == EditType.BACKGROUND) {
            currentMapImage = mapMaker.currentMapBg;
        } else if (mapMaker.editType == EditType.MOVE_MAP) {
            currentMapImage = mapMaker.currentMapMove;
        } else if (mapMaker.editType == EditType.AREA_MAP) {
            currentMapImage = mapMaker.currentMapArea;
        }

        int width = bx - tx + 1;
        int height = by - ty + 1;
        copiedTiles = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        copiedTiles.setRGB(0, 0, width, height,
                currentMapImage.getRGB(tx, ty, width, height, null, 0, width),
                0, width);

        if (!mapMaker.mntmPaste.isEnabled()) {
            mapMaker.mntmPaste.setEnabled(true);
        }
    }

    public void cut() {
        copy();

        int val = Integer.parseInt(mapMaker.tileList.getModel().getElementAt(0).getDescription());
        for (int i = tx; i <= bx; i++) {
            for (int j = ty; j <= by; j++) {
                mapMaker.setTile(i, j, val);
            }
        }
    }

    public void paste() {
        paste = true;
        deselect();
    }
}
