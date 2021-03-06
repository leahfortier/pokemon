package gui;

import draw.ImageUtils;
import util.file.FileIO;
import util.file.Folder;

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

    protected final String folderPath;
    private final float scale;
    private final Map<String, BufferedImage> map;

    public TileSet(String folderPath) {
        this(folderPath, 1.0f);
    }

    public TileSet(String folderPath, float scale) {
        this.folderPath = folderPath;
        this.scale = scale;

        this.map = new HashMap<>();
    }

    public BufferedImage getTile(String imageName) {
        if (!map.containsKey(imageName)) {
            File file = FileIO.newFile(this.folderPath + imageName + ".png");
            try {
                BufferedImage image = ImageUtils.read(file, scale);
                map.put(imageName, image);
            } catch (IOException exception) {
                return IMAGE_NOT_FOUND;
            }
        }

        return map.get(imageName);
    }

    // TODO: I still have no idea what the >> 24 shit means
    public static boolean isValidMapTile(int val) {
        return val != INVALID_RGB && (val >> 24) != 0;
    }
}
