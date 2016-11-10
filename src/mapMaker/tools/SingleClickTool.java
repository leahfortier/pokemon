package mapMaker.tools;

import map.entity.npc.NPCEntityData;
import mapMaker.MapMaker;
import mapMaker.MapMaker.EditType;
import mapMaker.TileMap.TileType;
import mapMaker.TriggerModelType;
import util.DrawUtils;
import util.Point;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class SingleClickTool extends Tool {
    public SingleClickTool(final MapMaker mapMaker) {
        super(mapMaker);
    }

    @Override
    public void click(Point clickedLocation) {
        if (mapMaker.tileList.isSelectionEmpty()) {
            return;
        }

        Point location = DrawUtils.getLocation(clickedLocation, mapMaker.getMapLocation());
        System.out.println("click: " + clickedLocation);

        int val = Integer.parseInt(mapMaker.tileList.getSelectedValue().getDescription());
        mapMaker.setTile(location, val);

        if (mapMaker.editType == EditType.TRIGGERS) {
            mapMaker.triggerData.clearPlaceableTrigger();
            mapMaker.toolList.setSelectedIndex(3); // TODO
        }
    }

    @Override
    public void draw(Graphics g) {
        Point location = DrawUtils.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());
        DrawUtils.outlineTileRed(g, location, mapMaker.getMapLocation());

        if (mapMaker.tileList.isSelectionEmpty()) {
            return;
        }

        // Show preview image for normal map tiles
        if (mapMaker.editType == EditType.BACKGROUND || mapMaker.editType == EditType.FOREGROUND) {
            int val = Integer.parseInt(mapMaker.tileList.getSelectedValue().getDescription());
            BufferedImage image = mapMaker.getTileFromSet(TileType.MAP, val);
            if (image != null) {
                DrawUtils.drawTileImage(g, image, location, mapMaker.getMapLocation());
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
                DrawUtils.drawTileImage(g, image, location, mapMaker.getMapLocation());
            }
        }
    }

    public String toString() {
        return "Single";
    }
}
