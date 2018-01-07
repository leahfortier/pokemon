package mapMaker.model;

import mapMaker.MapMaker;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;

public abstract class MapMakerModel {
    private static final Map<TileModelType, MapMakerModel> modelMap = createModelMap();

    private final int blankTileIndex;

    MapMakerModel(int blankTileIndex) {
        this.blankTileIndex = blankTileIndex;
    }

    public int getBlankTileIndex() {
        return this.blankTileIndex;
    }

    public abstract DefaultListModel<ImageIcon> getListModel();
    public abstract void reload(MapMaker mapMaker);
    public abstract boolean newTileButtonEnabled();

    // Should be overridden by subclasses which return true to newTileButtonEnabled
    public void newTileButtonPressed(MapMaker mapMaker) {}

    // Additional draw actions can be overridden
    public void draw(Graphics2D g, MapMaker mapMaker) {}

    private static Map<TileModelType, MapMakerModel> createModelMap() {
        final Map<TileModelType, MapMakerModel> modelMap = new EnumMap<>(TileModelType.class);
        for (final TileModelType modelType : TileModelType.values()) {
            modelMap.put(modelType, modelType.modelCreator.createModel());
        }

        return modelMap;
    }

    public static MapMakerModel getMapMakerModel(TileModelType modelType) {
        return modelMap.get(modelType);
    }

    public static TileModel getTileModel() {
        return (TileModel)getMapMakerModel(TileModelType.TILE);
    }

    public static AreaModel getAreaModel() {
        return (AreaModel)getMapMakerModel(TileModelType.AREA);
    }

    public static void reloadModels(MapMaker mapMaker) {
        for (MapMakerModel model : modelMap.values()) {
            model.reload(mapMaker);
        }
    }

    public enum TileModelType {
        AREA(AreaModel::new),
        MOVE(MoveModel::new),
        TILE(TileModel::new),
        TRIGGER(TriggerModel::new);

        private final ModelCreator modelCreator;

        TileModelType(ModelCreator modelCreator) {
            this.modelCreator = modelCreator;
        }

        @FunctionalInterface
        private interface ModelCreator {
            MapMakerModel createModel();
        }
    }
}
