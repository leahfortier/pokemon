package mapMaker;

import main.Global;
import map.MapMetaData.MapDataType;
import mapMaker.data.MapMakerTriggerData;
import mapMaker.model.AreaModel;
import mapMaker.model.EditMode.EditType;
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

// Metadata for the current map being edited
public class EditMapMetaData {
    private String currentMapName;
    private Dimension currentMapSize;

    private BufferedImage currentMapFg;
    private BufferedImage currentMapBg;
    private BufferedImage currentMapMove;
    private BufferedImage currentMapArea;

    public MapMakerTriggerData triggerData;

    private Composite alphaComposite;
    private Composite defaultComposite;

    public boolean saved;

    public EditMapMetaData(MapMaker mapMaker) {
        this.saved = true;
    }

    public String getMapName() {
        return this.currentMapName;
    }

    public Dimension getDimension() {
        return this.currentMapSize;
    }

    public boolean isSaved() {
        return saved && (triggerData != null && triggerData.isSaved());
    }

    public void createNewMap(MapMaker mapMaker, String mapName) {
        currentMapName = mapName;

        currentMapBg = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        currentMapFg = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        currentMapMove = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        currentMapArea = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

        // TODO: Maybe this should be a constant for the default size?
        currentMapSize = new Dimension(10, 10);

        Graphics g = currentMapMove.getGraphics();
        g.setColor(Color.BLACK); // TODO: Immovable?
        g.fillRect(0, 0, currentMapSize.width, currentMapSize.height);
        g.dispose();

        // Create empty trigger data structure
        triggerData = new MapMakerTriggerData(mapMaker);

        save(mapMaker);
    }

    public void save(MapMaker mapMaker) {
        if (currentMapBg == null) {
            return;
        }

        final String mapFolderPath = mapMaker.getMapFolderName(currentMapName);
        FileIO.createFolder(mapFolderPath);

        File mapBgFile = new File(mapFolderPath + currentMapName + "_bg.png");
        File mapFgFile = new File(mapFolderPath + currentMapName + "_fg.png");
        File mapMoveFile = new File(mapFolderPath + currentMapName + "_move.png");
        File mapAreaFile = new File(mapFolderPath + currentMapName + "_area.png");

        FileIO.writeImage(currentMapBg, mapBgFile);
        FileIO.writeImage(currentMapFg, mapFgFile);
        FileIO.writeImage(currentMapMove, mapMoveFile);
        FileIO.writeImage(currentMapArea, mapAreaFile);

        // Save all triggers
        triggerData.saveTriggers(mapMaker.getMapTextFile(currentMapName));

        saved = true;
    }


    public void loadPreviousMap(MapMaker mapMaker, String mapName, AreaModel areaModel) {
        currentMapName = mapName;

        final String mapFolderPath = mapMaker.getMapFolderName(currentMapName);

        // TODO: Can use that other function but not doing it because this will likely all get rewritten anyways
        File mapTextFile = new File(mapFolderPath + currentMapName + ".txt");

        // TODO: Will likely want an object to hold these
        File mapBgImageFile = new File(mapFolderPath + currentMapName + "_bg.png");
        File mapFgImageFile = new File(mapFolderPath + currentMapName + "_fg.png");
        File mapMoveImageFile = new File(mapFolderPath + currentMapName + "_move.png");
        File mapAreaImageFile = new File(mapFolderPath + currentMapName + "_area.png");

        currentMapBg = FileIO.readImage(mapBgImageFile);
        currentMapFg = FileIO.readImage(mapFgImageFile);
        currentMapMove = FileIO.readImage(mapMoveImageFile);

        // Check to see if area file exists
        if (mapAreaImageFile.exists()) {
            currentMapArea = FileIO.readImage(mapAreaImageFile);

            for (int x = 0; x < currentMapBg.getWidth(); ++x) {
                for (int y = 0; y < currentMapBg.getHeight(); ++y) {
                    int rgb = currentMapArea.getRGB(x, y);
                    areaModel.updateExistingAreas(rgb);
                }
            }
        }
        // If file doesn't exist, create it instead of crashing.
        else {
            currentMapArea = new BufferedImage(currentMapBg.getWidth(), currentMapBg.getHeight(), BufferedImage.TYPE_INT_ARGB);
        }

        currentMapSize = new Dimension(currentMapBg.getWidth(), currentMapBg.getHeight());

        triggerData = new MapMakerTriggerData(mapMaker, mapTextFile);
        this.saved = true;
    }

    // Checks if the current map needs to be resized based on the input location
    // If so, will return the delta to the new current location
    public Point checkNewDimension(Point location) {

        // In bounds -- no need to resize
        if (location.inBounds(currentMapSize)) {
            return new Point();
        }

        Dimension newDimension = location.maximizeDimension(currentMapSize);
        int newWidth = newDimension.width;
        int newHeight = newDimension.height;

        Point delta = Point.negate(location).lowerBound();
        int startX = delta.x;
        int startY = delta.y;

        System.out.println("New " + newWidth + " " + newHeight);
        System.out.println("Start " + delta);

        BufferedImage tmpBg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        BufferedImage tmpFg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        BufferedImage tmpMove = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        BufferedImage tmpArea = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics g = tmpMove.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, newWidth, newHeight);
        g.dispose();

        tmpBg.setRGB(startX, startY, currentMapSize.width, currentMapSize.height,
                currentMapBg.getRGB(0, 0, currentMapSize.width, currentMapSize.height, null, 0, currentMapSize.width), 0, currentMapSize.width);
        tmpFg.setRGB(startX, startY, currentMapSize.width, currentMapSize.height,
                currentMapFg.getRGB(0, 0, currentMapSize.width, currentMapSize.height, null, 0, currentMapSize.width), 0, currentMapSize.width);
        tmpMove.setRGB(startX, startY, currentMapSize.width, currentMapSize.height,
                currentMapMove.getRGB(0, 0, currentMapSize.width, currentMapSize.height, null, 0, currentMapSize.width), 0, currentMapSize.width);
        tmpArea.setRGB(startX, startY, currentMapSize.width, currentMapSize.height,
                currentMapArea.getRGB(0, 0, currentMapSize.width, currentMapSize.height, null, 0, currentMapSize.width), 0, currentMapSize.width);

        currentMapBg = tmpBg;
        currentMapFg = tmpFg;
        currentMapMove = tmpMove;
        currentMapArea = tmpArea;

        // Update trigger data type
        triggerData.moveTriggerData(startX, startY, newDimension);
        currentMapSize = newDimension;

        return delta;
    }

    public boolean hasMap() {
        return this.currentMapBg != null;
    }

    public BufferedImage getImage(MapDataType dataType) {
        switch (dataType) {
            case BACKGROUND:
                return this.currentMapBg;
            case FOREGROUND:
                return this.currentMapFg;
            case MOVE:
                return this.currentMapMove;
            case AREA:
                return this.currentMapArea;
        }

        Global.error("Unknown data type " + dataType.name());
        return this.currentMapBg;
    }

    public void setTile(MapDataType dataType, Point location, int val) {
        this.getImage(dataType).setRGB(location.x, location.y, val);
        this.saved = false;
    }

    public int getTile(Point location, MapDataType dataType) {
        // TODO: should 0 be a constant? should it correspond to the current blank tile index? what is happening actually
        if (dataType == null || !location.inBounds(this.currentMapSize)) {
            return 0;
        }

        return this.getImage(dataType).getRGB(location.x, location.y);
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
}
