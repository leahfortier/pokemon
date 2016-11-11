package mapMaker.tools;

import map.entity.npc.NPCEntityData;
import mapMaker.MapMaker;
import mapMaker.model.EditMode.EditType;
import mapMaker.model.TileModel.TileType;
import mapMaker.model.TriggerModel.TriggerModelType;
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
        if (mapMaker.isTileSelectionEmpty()) {
            return;
        }

        Point location = DrawUtils.getLocation(clickedLocation, mapMaker.getMapLocation());
        System.out.println("click: " + clickedLocation);

        int val = mapMaker.getSelectedTile();
        mapMaker.setTile(location, val);

        if (mapMaker.isEditType(EditType.TRIGGERS)) {
            mapMaker.clearPlaceableTrigger();
            mapMaker.setTool(ToolType.TRIGGER);
        }
    }

    @Override
    public void draw(Graphics g) {
        Point location = DrawUtils.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());
        DrawUtils.outlineTileRed(g, location, mapMaker.getMapLocation());

        if (mapMaker.isTileSelectionEmpty()) {
            return;
        }

        // Show preview image for normal map tiles
        if (mapMaker.isEditType(EditType.BACKGROUND) || mapMaker.isEditType(EditType.FOREGROUND)) {
            int val = mapMaker.getSelectedTile();
            BufferedImage image = mapMaker.getTileFromSet(TileType.MAP, val);
            if (image != null) {
                DrawUtils.drawTileImage(g, image, location, mapMaker.getMapLocation());
            }
        }
        // Show preview image for current trigger
        else if (mapMaker.isEditType(EditType.TRIGGERS)) {

            TriggerModelType type = TriggerModelType.getModelTypeFromIndex(mapMaker.getSelectedTileIndex());

            BufferedImage image;
            if (type == TriggerModelType.NPC) {
                // TODO
                NPCEntityData npc = (NPCEntityData) mapMaker.getPlaceableTrigger().entity;
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
