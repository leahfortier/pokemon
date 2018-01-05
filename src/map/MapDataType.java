package map;

import util.FileIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;

public enum MapDataType {
    BACKGROUND("_bg"),
    BACK_FOREGROUND("_bfg"),
    FOREGROUND("_fg"),
    TALL_GRASS("_3dl"),
    MOVE("_move"),
    AREA("_area");
    
    private final String suffix;
    
    MapDataType(String suffix) {
        this.suffix = suffix;
    }
    
    public String getImageName(String mapName) {
        return mapName + this.suffix + ".png";
    }
    
    public static Map<MapDataType, BufferedImage> getImageMap(String beginFilePath, String mapName) {
        final Map<MapDataType, BufferedImage> imageMap = new EnumMap<>(MapDataType.class);
        for (MapDataType dataType : MapDataType.values()) {
            String fileName = beginFilePath + dataType.getImageName(mapName);
            File imageFile = new File(fileName);
            imageMap.put(dataType, FileIO.readImage(imageFile));
        }
        
        return imageMap;
    }
}
