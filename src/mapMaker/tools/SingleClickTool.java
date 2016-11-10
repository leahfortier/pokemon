package mapMaker.tools;

import map.entity.npc.NPCEntityData;
import util.Point;
import mapMaker.MapMaker;
import mapMaker.MapMaker.EditType;
import mapMaker.MapMaker.TileType;
import mapMaker.TriggerModelType;
import util.DrawMetrics;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class SingleClickTool extends Tool {
    public SingleClickTool(final MapMaker mapMaker) {
        super(mapMaker);
    }

    public void click(int x, int y) {
        if (mapMaker.tileList.isSelectionEmpty()) {
            return;
        }

        Point location = DrawMetrics.getLocation(new Point(x, y), mapMaker.getMapLocation());
        System.out.println("click: " + x + " " + y);

        int val = Integer.parseInt(mapMaker.tileList.getSelectedValue().getDescription());
        mapMaker.setTile(location, val);

        if (mapMaker.editType == EditType.TRIGGERS) {
            mapMaker.triggerData.clearPlaceableTrigger();
            mapMaker.toolList.setSelectedIndex(3); // TODO
        }
    }

    public String toString() {
        return "Single";
    }

    public void draw(Graphics g) {
        Point location = DrawMetrics.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());
        DrawMetrics.outlineTileRed(g, location, mapMaker.getMapLocation());

        if (mapMaker.tileList.isSelectionEmpty()) {
            return;
        }

        // Show preview image for normal map tiles
        if (mapMaker.editType == EditType.BACKGROUND || mapMaker.editType == EditType.FOREGROUND) {
            int val = Integer.parseInt(mapMaker.tileList.getSelectedValue().getDescription());
            BufferedImage image = mapMaker.getTileFromSet(TileType.MAP, val);
            if (image != null) {
                DrawMetrics.drawTileImage(g, image, location, mapMaker.getMapLocation());
            }
        }
        // Show preview image for current trigger
        else if (mapMaker.editType == EditType.TRIGGERS) {

            TriggerModelType type = TriggerModelType.getModelTypeFromIndex(mapMaker.tileList.getSelectedIndex());

            BufferedImage image;
            if (type == TriggerModelType.NPC) {
                // TODO
                NPCEntityData npc = (NPCEntityData) mapMaker.triggerData.getPlaceableTrigger().entity;
                image = mapMaker.getTileFromSet(TileType.TRAINER, 12 * npc.spriteIndex + 1 + npc.defaultDirection.ordinal());
            }
            else {
                image = type.getImage(mapMaker);
            }

            if (image != null) {
                DrawMetrics.drawTileImage(g, image, location, mapMaker.getMapLocation());
            }
        }
    }
}
