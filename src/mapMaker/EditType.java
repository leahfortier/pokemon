package mapMaker;

import map.MapDataType;
import mapMaker.model.MapMakerModel;
import mapMaker.model.MapMakerModel.TileModelType;

public enum EditType {
    BACKGROUND(MapDataType.BACKGROUND, TileModelType.TILE),
    BACK_FOREGROUND(MapDataType.BACK_FOREGROUND, TileModelType.TILE),
    FOREGROUND(MapDataType.FOREGROUND, TileModelType.TILE),
    TALL_GRASS(MapDataType.TALL_GRASS, TileModelType.TILE),
    MOVE_MAP(MapDataType.MOVE, TileModelType.MOVE),
    AREA_MAP(MapDataType.AREA, TileModelType.AREA),
    TRIGGERS(null, TileModelType.TRIGGER);

    private final MapDataType dataType;
    private final TileModelType modelType;

    EditType(MapDataType dataType, TileModelType modelType) {
        this.dataType = dataType;
        this.modelType = modelType;
    }

    public MapDataType getDataType() {
        return this.dataType;
    }

    public TileModelType getModelType() {
        return this.modelType;
    }

    public MapMakerModel getModel() {
        return MapMakerModel.getMapMakerModel(this.modelType);
    }
}
