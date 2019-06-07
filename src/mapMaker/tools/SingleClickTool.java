package mapMaker.tools;

import draw.TileUtils;
import map.entity.movable.MovableEntity;
import mapMaker.EditType;
import mapMaker.MapMaker;
import mapMaker.model.TileModel.TileType;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.LocationTriggerMatcher;
import pattern.map.NPCMatcher;
import util.Point;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

class SingleClickTool extends Tool {
    private Point lastLocation;
    private int lastVal;
    private EditType lastEditType;
    private LocationTriggerMatcher lastTrigger;

    SingleClickTool(final MapMaker mapMaker) {
        super(mapMaker, ToolType.SINGLE_CLICK);
    }

    @Override
    public void click(Point clickedLocation) {
        if (!mapMaker.hasSelectedTile()) {
            return;
        }

        Tool.lastUsedTool = this;

        Point location = TileUtils.getLocation(clickedLocation, mapMaker.getMapLocation());
        lastLocation = location;
        System.out.println("click: " + clickedLocation);

        lastVal = mapMaker.getTile(location, mapMaker.getEditType().getDataType());
        int val = mapMaker.getSelectedTile();
        mapMaker.setTile(location, val);

        lastEditType = mapMaker.getEditType();

        if (mapMaker.isEditType(EditType.TRIGGERS)) {
            lastTrigger = mapMaker.getPlaceableTrigger();
            mapMaker.clearPlaceableTrigger();
            mapMaker.setTool(ToolType.TRIGGER);
        }
    }

    @Override
    public void draw(Graphics g) {
        Point location = TileUtils.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());
        TileUtils.outlineTileRed(g, location, mapMaker.getMapLocation());

        if (!mapMaker.hasSelectedTile()) {
            return;
        }

        // Show preview image for normal map tiles
        if (mapMaker.isEditType(EditType.BACKGROUND) || mapMaker.isEditType(EditType.FOREGROUND)) {
            int val = mapMaker.getSelectedTile();
            BufferedImage image = mapMaker.getTileFromSet(TileType.MAP, val);
            if (image != null) {
                TileUtils.drawTileImage(g, image, location, mapMaker.getMapLocation());
            }
        }
        // Show preview image for current trigger
        else if (mapMaker.isEditType(EditType.TRIGGERS)) {

            TriggerModelType type = TriggerModelType.getModelTypeFromIndex(mapMaker.getSelectedTileIndex());

            BufferedImage image;
            if (type == TriggerModelType.NPC) {
                NPCMatcher npc = (NPCMatcher)mapMaker.getPlaceableTrigger();
                image = mapMaker.getTileFromSet(TileType.TRAINER, MovableEntity.getTrainerSpriteIndex(npc.getSpriteIndex(), npc.getDirection()));
            } else {
                image = type.getImage(mapMaker);
            }

            if (image != null) {
                TileUtils.drawTileImage(g, image, location, mapMaker.getMapLocation());
            }
        }
    }

    @Override
    public void undo() {
        if (lastLocation != null && lastEditType != null) {
            if (lastEditType == EditType.TRIGGERS) {
                mapMaker.getTriggerData().removeTrigger(lastTrigger);
            } else {
                mapMaker.setTile(lastLocation, lastVal, lastEditType);
            }
            lastLocation = null;
            lastEditType = null;
            lastTrigger = null;
        }
    }

    @Override
    public String toString() {
        return "Single";
    }
}
