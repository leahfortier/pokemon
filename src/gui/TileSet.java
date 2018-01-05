package gui;

import draw.ImageUtils;
import util.FileIO;
import util.Folder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TileSet {
    public static final int INVALID_RGB = -1000;
    
    public static final BufferedImage TINY_POKEBALL = FileIO.readImage(Folder.IMAGES + "TinyPokeball.png");
    public static final BufferedImage ITEM_POKEBALL = FileIO.readImage(Folder.IMAGES + "ItemPokeball.png");
    public static final BufferedImage TM_ITEM_POKEBALL = FileIO.readImage(Folder.IMAGES + "TMItemPokeball.png");
    public static final BufferedImage STAR_SPRITE = FileIO.readImage(Folder.IMAGES + "starsies.png");
    
    protected static final BufferedImage IMAGE_NOT_FOUND = FileIO.readImage(Folder.IMAGES + "imageNotFound.png");
    
    private Map<String, BufferedImage> map;
    private float scale;
    protected String folderPath;
    
    public TileSet(String folderPath) {
        this(folderPath, 1.0f);
    }
    
    public TileSet(String folderPath, float scale) {
        this.folderPath = folderPath;
        this.scale = scale;
        
        this.map = new HashMap<>();
    }
    
    // TODO: I still have no idea what the >> 24 shit means
    public static boolean isValidMapTile(int val) {
        return val != INVALID_RGB && (val >> 24) != 0;
    }
    
    public BufferedImage getTile(String imageName) {
        if (!map.containsKey(imageName)) {
            File file = new File(this.folderPath + imageName + ".png");
            try {
                BufferedImage image = ImageIO.read(file);
                image = ImageUtils.scaleImage(image, scale);
                map.put(imageName, image);
            }
            catch (IOException exception) {
                return IMAGE_NOT_FOUND;
            }
        }
        
        return map.get(imageName);
    }
}
