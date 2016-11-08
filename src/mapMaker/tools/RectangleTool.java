package mapMaker.tools;

import mapMaker.MapMaker;
import mapMaker.MapMaker.EditType;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class RectangleTool extends Tool {
    private int startX, startY;
    private boolean pressed = false;

    public RectangleTool(MapMaker mapMaker) {
        super(mapMaker);
    }

    public void released(int x, int y) {
        if (mapMaker.tileList.isSelectionEmpty() || !pressed) {
            return;
        }

        int mhx = (int) Math.floor((mapMaker.mouseHoverX - mapMaker.mapX) * 1.0 / MapMaker.tileSize);
        int mhy = (int) Math.floor((mapMaker.mouseHoverY - mapMaker.mapY) * 1.0 / MapMaker.tileSize);

        pressed = false;

        int tx = Math.min(startX, mhx);
        int ty = Math.min(startY, mhy);
        int bx = Math.max(startX, mhx);
        int by = Math.max(startY, mhy);

        int width = bx - tx;
        int height = by - ty;

        int val = Integer.parseInt(mapMaker.tileList.getSelectedValue().getDescription());
        for (int i = 0; i <= width; i++) {
            for (int j = 0; j <= height; j++) {
                Point delta = mapMaker.setTile(tx + i, ty + j, val);

                if (delta.x != 0) {
                    tx += delta.x;
                }

                if (delta.y != 0) {
                    ty += delta.y;
                }
            }
        }

        if (mapMaker.editType == EditType.TRIGGERS) {
            mapMaker.triggerData.clearPlaceableTrigger();
            mapMaker.toolList.setSelectedIndex(3);
        }
    }

    public void pressed(int x, int y) {
        if (mapMaker.tileList.isSelectionEmpty()) {
            return;
        }

        startX = (int) Math.floor((x - mapMaker.mapX) * 1.0 / MapMaker.tileSize);
        startY = (int) Math.floor((y - mapMaker.mapY) * 1.0 / MapMaker.tileSize);

        pressed = true;
    }

    public void drag(int x, int y) {
    }

    public void draw(Graphics g) {
        int mhx = (int) Math.floor((mapMaker.mouseHoverX - mapMaker.mapX) * 1.0 / MapMaker.tileSize);
        int mhy = (int) Math.floor((mapMaker.mouseHoverY - mapMaker.mapY) * 1.0 / MapMaker.tileSize);

        g.setColor(Color.red);
        if (!pressed) {
            g.drawRect(mhx * MapMaker.tileSize + mapMaker.mapX, mhy * MapMaker.tileSize + mapMaker.mapY, MapMaker.tileSize, MapMaker.tileSize);
        } else {
            int tx = Math.min(startX, mhx);
            int ty = Math.min(startY, mhy);
            int bx = Math.max(startX, mhx);
            int by = Math.max(startY, mhy);

            g.drawRect(tx * MapMaker.tileSize + mapMaker.mapX, ty * MapMaker.tileSize + mapMaker.mapY, MapMaker.tileSize * (bx - tx + 1), MapMaker.tileSize * (by - ty + 1));
        }
    }

    public String toString() {
        return "Rectangle";
    }

    public void reset() {
        pressed = false;
    }
}
