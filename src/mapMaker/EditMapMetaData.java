package mapMaker;

import map.MapMetaData.MapDataType;
import mapMaker.data.MapMakerTriggerData;
import mapMaker.model.AreaModel;
import mapMaker.model.EditMode.EditType;
import mapMaker.model.MoveModel.MoveModelType;
import mapMaker.model.TileModel;
import mapMaker.model.TileModel.TileType;
import util.DrawUtils;
import util.FileIO;
import util.Point;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
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
            this.currentMap.put(dataType, DrawUtils.createNewImage(this.currentMapSize));
        }

        Graphics g = this.getMapImage(MapDataType.MOVE).getGraphics();
        g.setColor(MoveModelType.IMMOVABLE.getColor());
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

        final String mapFolderPath = mapMaker.getMapFolderName(currentMapName);
        FileIO.createFolder(mapFolderPath);

        for (MapDataType dataType : MapDataType.values()) {
            File mapFile = new File(mapFolderPath + dataType.getImageName(this.currentMapName));
            FileIO.writeImage(this.getMapImage(dataType), mapFile);
        }

        // Save all triggers
        triggerData.saveTriggers(mapMaker.getMapTextFile(currentMapName));

        saved = true;
    }


    public void loadPreviousMap(MapMaker mapMaker, String mapName, AreaModel areaModel) {
        this.currentMapName = mapName;

        final String mapFolderPath = mapMaker.getMapFolderName(currentMapName);
        File mapTextFile = new File(mapFolderPath + currentMapName + ".txt");

        for (MapDataType dataType : MapDataType.values()) {
            File mapFile = new File(mapFolderPath + dataType.getImageName(this.currentMapName));
            this.currentMap.put(dataType, FileIO.readImage(mapFile));
        }

        BufferedImage mapBackground = this.getMapImage(MapDataType.BACKGROUND);
        this.currentMapSize = new Dimension(mapBackground.getWidth(), mapBackground.getHeight());

        this.triggerData = new MapMakerTriggerData(mapMaker, mapTextFile);
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

        Point delta = Point.negate(location).lowerBound();

        System.out.println("New " + currentMapSize.width + " " + currentMapSize.height);
        System.out.println("Start " + delta);

        this.resetMaps();

        for (Entry<MapDataType, BufferedImage> entry : currentMap.entrySet()) {
            MapDataType dataType = entry.getKey();
            BufferedImage newMapImage = entry.getValue();

            BufferedImage previousMapImage = previousMap.get(dataType);
            int[] rgb = previousMapImage.getRGB(0, 0, previousDimension.width, previousDimension.height, null, 0, previousDimension.width);

            newMapImage.setRGB(delta.x, delta.y, previousDimension.width, previousDimension.height, rgb, 0, previousDimension.width);
        }

        // Update trigger data type
        triggerData.moveTriggerData(delta, currentMapSize.width);

        return delta;
    }

    public boolean hasMap() {
        return this.currentMap.containsKey(MapDataType.BACKGROUND);
    }

    public void setTile(MapDataType dataType, Point location, int val) {
        this.getMapImage(dataType).setRGB(location.x, location.y, val);
        this.saved = false;
    }

    public int getTile(Point location, MapDataType dataType) {
        // TODO: should 0 be a constant? should it correspond to the current blank tile index? what is happening actually
        if (dataType == null || !location.inBounds(this.currentMapSize)) {
            return 0;
        }

        return this.getMapImage(dataType).getRGB(location.x, location.y);
    }

    private void drawTiles(Graphics2D g2d, Point mapLocation, MapDataType type, TileModel tileModel, Composite composite) {
        g2d.setComposite(composite);

        for (int y = 0; y < currentMapSize.height; y++) {
            for (int x = 0; x < currentMapSize.width; x++) {
                Point location = new Point(x, y);
                int val = getTile(location, type);

                if (type == MapDataType.MOVE || type == MapDataType.AREA) {
                    DrawUtils.fillTile(g2d, location, mapLocation, new Color(val, true));
                }
                else if (tileModel.containsTile(TileType.MAP, val)) {
                    BufferedImage image = tileModel.getTile(TileType.MAP, val);
                    DrawUtils.drawTileImage(g2d, image, location, mapLocation);
                }
            }
        }
    }

    public void drawMap(Graphics g, Point mapLocation, EditType editType, TileModel tileModel) {
        Graphics2D g2d = (Graphics2D)g;
        if (alphaComposite == null) {
            defaultComposite = g2d.getComposite();
            alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f);
        }

        switch (editType) {
            // Drawing of area map handled in MOVE_MAP case.
            case AREA_MAP:
            case MOVE_MAP:
                drawTiles(g2d, mapLocation, editType.getDataType(), tileModel, defaultComposite);
                drawTiles(g2d, mapLocation, MapDataType.BACKGROUND, tileModel, alphaComposite);
                drawTiles(g2d, mapLocation, MapDataType.FOREGROUND, tileModel, alphaComposite);
                break;
            case TRIGGERS:
            case BACKGROUND:
                drawTiles(g2d, mapLocation, MapDataType.BACKGROUND, tileModel, defaultComposite);
                drawTiles(g2d, mapLocation, MapDataType.FOREGROUND, tileModel, alphaComposite);
                break;
            case FOREGROUND:
                drawTiles(g2d, mapLocation, MapDataType.BACKGROUND, tileModel, alphaComposite);
                drawTiles(g2d, mapLocation, MapDataType.FOREGROUND, tileModel, defaultComposite);
                break;
        }


        if (editType != EditType.TRIGGERS) {
            // Draw all trigger items at half transparency.
            g2d.setComposite(alphaComposite);
        }
        else {
            g2d.setComposite(defaultComposite);
        }

        // Draw all trigger items.
        triggerData.drawTriggers(g2d, mapLocation);

        g2d.setComposite(defaultComposite);
    }

    public MapMakerTriggerData getTriggerData() {
        return this.triggerData;
    }
}
