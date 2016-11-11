package mapMaker.model;

import map.MapMetaData.MapDataType;
import mapMaker.EditMapMetaData;
import mapMaker.MapMaker;
import util.Point;

public class EditMode {

    private final TileModel tileModel;
    private final AreaModel areaModel;
    private final MoveModel moveModel;
    private final TriggerModel triggerModel;

    private EditType editType;

    public enum EditType {
        BACKGROUND(MapDataType.BACKGROUND, editMode -> editMode.tileModel),
        FOREGROUND(MapDataType.FOREGROUND, editMode -> editMode.tileModel),
        MOVE_MAP(MapDataType.MOVE, editMode -> editMode.moveModel),
        AREA_MAP(MapDataType.AREA, editMode -> editMode.areaModel),
        TRIGGERS(null, editMode -> editMode.triggerModel);

        private final MapDataType dataType;
        private final ModelGetter modelGetter;

        EditType(MapDataType dataType, ModelGetter modelGetter) {
            this.dataType = dataType;
            this.modelGetter = modelGetter;
        }

        private interface ModelGetter {
            public MapMakerModel getModel(EditMode editMode);
        }

        private MapMakerModel getModel(EditMode editMode) {
            return this.modelGetter.getModel(editMode);
        }

        public MapDataType getDataType() {
            return this.dataType;
        }
    }

    public EditMode(MapMaker mapMaker) {
        this.tileModel = new TileModel(mapMaker);
        this.areaModel = new AreaModel(mapMaker);
        this.moveModel = new MoveModel(mapMaker);
        this.triggerModel = new TriggerModel(mapMaker);

        this.editType = EditType.BACKGROUND;
    }

    public void setEditType(EditType editType) {
        this.editType = editType;
    }

    public EditType getEditType() {
        return this.editType;
    }

    public MapMakerModel getModel() {
        return this.editType.getModel(this);
    }

    public TileModel getTileModel() {
        return this.tileModel;
    }

    public AreaModel getAreaModel() {
        return this.areaModel;
    }

    public void reload(MapMaker mapMaker) {
        tileModel.reload(mapMaker);
        areaModel.reload(mapMaker);
        moveModel.reload(mapMaker);
        triggerModel.reload(mapMaker);
    }

    // Set the tile at the specified location for the current edit type
    // Returns whether or not the tile selection should be cleared afterwards
    public boolean setTile(EditMapMetaData mapData, Point location, int val) {
        if (this.editType == EditType.TRIGGERS) {
            mapData.triggerData.placeTrigger(location);
            return true;
        } else {
            mapData.setTile(this.editType.dataType, location, val);
            return false;
        }
    }
}
