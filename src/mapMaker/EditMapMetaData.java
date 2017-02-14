package mapMaker;

import draw.ImageUtils;
import draw.TileUtils;
import map.MapDataType;
import map.WalkType;
import mapMaker.model.MapMakerModel;
import mapMaker.model.TileModel;
import mapMaker.model.TileModel.TileType;
import util.FileIO;
import util.Point;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

// Metadata for the current map being edited
public class EditMapMetaData {
    private static final Dimension DEFAULT_MAP_SIZE = new Dimension(10, 10);

    private String currentMapName;
    private Dimension currentMapSize;

    private Map<MapDataType, BufferedImage> currentMap;

    private MapMakerTriggerData triggerData;

    private Composite alphaComposite;
    private Composite defaultComposite;

    public boolean saved;

    public EditMapMetaData() {
        this.currentMap = new EnumMap<>(MapDataType.class);
        this.saved = true;
    }

    public String getMapName() {
        return this.currentMapName;
    }

    public Dimension getDimension() {
        return this.currentMapSize;
    }

    public boolean isSaved() {
        return saved && (triggerData == null || triggerData.isSaved());
    }

    public BufferedImage getMapImage(MapDataType dataType) {
        return this.currentMap.get(dataType);
    }

    private void resetMaps() {
        for (MapDataType dataType : MapDataType.values()) {
            this.currentMap.put(dataType, ImageUtils.createNewImage(this.currentMapSize));
        }

        Graphics g = this.getMapImage(MapDataType.MOVE).getGraphics();
        g.setColor(WalkType.NOT_WALKABLE.getColor());
        g.fillRect(0, 0, currentMapSize.width, currentMapSize.height);
        g.dispose();
    }

    public void createNewMap(MapMaker mapMaker, String mapName) {
        currentMapName = mapName;

        currentMapSize = DEFAULT_MAP_SIZE;
        this.resetMaps();

        // Create empty trigger data structure
        triggerData = new MapMakerTriggerData(mapMaker);

        save(mapMaker);
    }

    public void save(MapMaker mapMaker) {
        if (!this.hasMap()) {
            return;
        }

        final String mapFolderPath = mapMaker.getMapFolderPath(currentMapName);
        FileIO.createFolder(mapFolderPath);

        for (MapDataType dataType : MapDataType.values()) {
            String mapFileName = mapFolderPath + dataType.getImageName(this.currentMapName);
            FileIO.writeImage(this.getMapImage(dataType), mapFileName);
        }

        // Save all triggers
        triggerData.saveTriggers(mapMaker.getMapTextFileName(currentMapName));

        saved = true;
    }

    public void loadPreviousMap(MapMaker mapMaker, String mapName) {
        this.currentMapName = mapName;

        final String mapFolderPath = mapMaker.getMapFolderPath(currentMapName);
        this.currentMap = MapDataType.getImageMap(mapFolderPath, currentMapName);

        MapMakerModel.getAreaModel().resetMap();

        BufferedImage mapBackground = this.getMapImage(MapDataType.BACKGROUND);
        this.currentMapSize = new Dimension(mapBackground.getWidth(), mapBackground.getHeight());

        String mapTextFileName = mapFolderPath + currentMapName + ".txt";
        this.triggerData = new MapMakerTriggerData(mapMaker, mapTextFileName);
        this.saved = true;
    }

    // Checks if the current map needs to be resized based on the input location
    // If so, will return the delta to the new current location
    public Point checkNewDimension(Point location) {

        // In bounds -- no need to resize
        if (location.inBounds(currentMapSize)) {
            return new Point();
        }

        Dimension previousDimension = currentMapSize;
        Map<MapDataType, BufferedImage> previousMap = new EnumMap<>(MapDataType.class);
        for (Entry<MapDataType, BufferedImage> entry : currentMap.entrySet()) {
            previousMap.put(entry.getKey(), entry.getValue());
        }

        currentMapSize = location.maximizeDimension(previousDimension);

        Point delta = Point.lowerBound(Point.negate(location));

        this.resetMaps();

        for (Entry<MapDataType, BufferedImage> entry : currentMap.entrySet()) {
            MapDataType dataType = entry.getKey();
            BufferedImage newMapImage = entry.getValue();

            BufferedImage previousMapImage = previousMap.get(dataType);
            int[] rgb = previousMapImage.getRGB(0, 0, previousDimension.width, previousDimension.height, null, 0, previousDimension.width);

            newMapImage.setRGB(delta.x, delta.y, previousDimension.width, previousDimension.height, rgb, 0, previousDimension.width);
        }

        // Update trigger data type
        triggerData.moveTriggerData(delta);

        return delta;
    }

    public boolean hasMap() {
        return this.currentMap.containsKey(MapDataType.BACKGROUND);
    }

    // Set the tile at the specified location for the current edit type
    // Returns whether or not the tile selection should be cleared afterwards
    public boolean setTile(EditType editType, Point location, int val) {
        if (editType == EditType.TRIGGERS) {
            this.triggerData.placeTrigger(location);
            return true;
        } else {
            // Not sure why I have to do new Color(val).getRGB() instead of just val, but it doesn't work like that so yeah
            this.getMapImage(editType.getDataType()).setRGB(location.x, location.y, new Color(val).getRGB());
            this.saved = false;
            return false;
        }
    }

    public int getTile(Point location, MapDataType dataType) {
        // TODO: should 0 be a constant? should it correspond to the current blank tile index? what is happening actually
        if (dataType == null || !location.inBounds(this.currentMapSize)) {
            return 0;
        }

        return this.getMapImage(dataType).getRGB(location.x, location.y);
    }

    private void drawTiles(Graphics2D g2d, Point mapLocation, MapDataType type, Composite composite) {
        g2d.setComposite(composite);

        TileModel tileModel = MapMakerModel.getTileModel();
        for (int y = 0; y < currentMapSize.height; y++) {
            for (int x = 0; x < currentMapSize.width; x++) {
                Point location = new Point(x, y);
                int val = getTile(location, type);

                if (type == MapDataType.MOVE || type == MapDataType.AREA) {
                    TileUtils.fillTile(g2d, location, mapLocation, new Color(val, true));
                }
                else if (type != null && tileModel.containsTile(TileType.MAP, val)) {
                    BufferedImage image = tileModel.getTile(TileType.MAP, val);
                    TileUtils.drawTileImage(g2d, image, location, mapLocation);
                }
            }
        }
    }

    public void drawMap(Graphics g, Point mapLocation, EditType editType) {
        Graphics2D g2d = (Graphics2D)g;
        if (alphaComposite == null) {
            defaultComposite = g2d.getComposite();
            alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f);
        }

        switch (editType) {
            // Drawing of area map handled in MOVE_MAP case.
            case AREA_MAP:
            case MOVE_MAP:
                drawTiles(g2d, mapLocation, editType.getDataType(), defaultComposite);
                drawTiles(g2d, mapLocation, MapDataType.BACKGROUND, alphaComposite);
                drawTiles(g2d, mapLocation, MapDataType.FOREGROUND, alphaComposite);
                break;
            case TRIGGERS:
            case BACKGROUND:
                drawTiles(g2d, mapLocation, MapDataType.BACKGROUND, defaultComposite);
                drawTiles(g2d, mapLocation, MapDataType.FOREGROUND, alphaComposite);
                break;
            case FOREGROUND:
                drawTiles(g2d, mapLocation, MapDataType.BACKGROUND, alphaComposite);
                drawTiles(g2d, mapLocation, MapDataType.FOREGROUND, defaultComposite);
                break;
        }


        if (editType != EditType.TRIGGERS) {
            // Draw all trigger items at half transparency.
            g2d.setComposite(alphaComposite);
        }
        else {
            g2d.setComposite(defaultComposite);
        }

        // Draw all trigger items
        if (triggerData != null) {
            triggerData.drawTriggers(g2d, mapLocation);
        }

        g2d.setComposite(defaultComposite);
    }

    public MapMakerTriggerData getTriggerData() {
        return this.triggerData;
    }
}
