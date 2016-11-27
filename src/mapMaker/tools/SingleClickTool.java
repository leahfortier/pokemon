package mapMaker.tools;

import map.entity.MovableEntity;
import mapMaker.MapMaker;
import mapMaker.EditType;
import mapMaker.model.TileModel.TileType;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.map.NPCMatcher;
import util.DrawUtils;
import util.Point;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

class SingleClickTool extends Tool {
    SingleClickTool(final MapMaker mapMaker) {
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
                NPCMatcher npc = (NPCMatcher) mapMaker.getPlaceableTrigger();
                image = mapMaker.getTileFromSet(TileType.TRAINER, MovableEntity.getTrainerSpriteIndex(npc.getSpriteIndex(), npc.getDirection()));
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
