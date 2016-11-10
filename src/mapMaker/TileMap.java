package mapMaker;

import util.DrawUtils;
import util.FileIO;
import util.FileName;
import util.Folder;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class TileMap {

    private final DefaultListModel<ImageIcon> tileListModel;

    private final Map<Integer, String> indexMap;
    private boolean saved;

    private final Map<Integer, BufferedImage> mapTileMap;
    private final Map<Integer, BufferedImage> mapMakerTileMap;
    private final Map<Integer, BufferedImage> trainerTileMap;
    private final Map<Integer, BufferedImage> itemTileMap;

    public TileMap(MapMaker mapMaker) {
        this.tileListModel = new DefaultListModel<>();

        this.indexMap = new HashMap<>();
        this.saved = true;

        this.mapTileMap = loadTiles(mapMaker, Folder.MAP_TILES, FileName.MAP_TILES_INDEX, this.tileListModel, this.indexMap, true);
        this.tileListModel.add(0, new ImageIcon(DrawUtils.fillImage(Color.MAGENTA), "0")); // TODO: I think magenta here is representing a blank tile, if so, this should be a constant

        this.trainerTileMap = loadTiles(mapMaker, Folder.TRAINER_TILES, FileName.TRAINER_TILES_INDEX);
        this.mapMakerTileMap = loadTiles(mapMaker, Folder.MAP_MAKER_TILES, FileName.MAP_MAKER_TILES_INDEX);
        this.itemTileMap = loadTiles(mapMaker, Folder.ITEM_TILES, FileName.ITEM_TILES_INDEX);
    }

    public enum TileType {
        ITEM(tileMap -> tileMap.itemTileMap),
        MAP(tileMap -> tileMap.mapTileMap),
        MAP_MAKER(tileMap -> tileMap.mapMakerTileMap),
        TRAINER(tileMap -> tileMap.trainerTileMap);

        private TileMapGetter tileMapGetter;

        TileType(TileMapGetter tileMapGetter) {
            this.tileMapGetter = tileMapGetter;
        }

        private interface TileMapGetter {
            Map<Integer, BufferedImage> getTileMap(TileMap tileMap);
        }

        private Map<Integer, BufferedImage> getTileMap(TileMap tileMap) {
            return this.tileMapGetter.getTileMap(tileMap);
        }
    }

    public DefaultListModel<ImageIcon> getModel() {
        return this.tileListModel;
    }

    private Map<Integer, BufferedImage> loadTiles(MapMaker mapMaker, String tileFolderName, String indexFileName) {
        return this.loadTiles(mapMaker, tileFolderName, indexFileName, null, null, false);
    }

    private Map<Integer, BufferedImage> loadTiles(
            MapMaker mapMaker,
            String tileFolderName,
            String indexFileName,
            DefaultListModel<ImageIcon> listModel,
            Map<Integer, String> indexMap,
            boolean resize) {
        File indexFile = new File(mapMaker.getPathWithRoot(indexFileName));

        Map<Integer, BufferedImage> tileMap = new HashMap<>();

        if (listModel != null) {
            listModel.clear();
        }

        if (indexFile.exists()) {
            Scanner in = FileIO.openFile(indexFile);
            while (in.hasNext()) {
                String name = in.next();
                int val = (int)Long.parseLong(in.next(), 16);

                File imageFile = new File(tileFolderName + name);
                if (!imageFile.exists()) {
//                    System.err.println("Could not find image " + imageFile.getName());
                    continue;
                }

                BufferedImage image = FileIO.readImage(imageFile);
                if (resize) {
                    // TODO: What is this doing?
                    image = image.getSubimage(
                            0,
                            0,
                            Math.min(image.getWidth(), MapMaker.tileSize*3),
                            Math.min(image.getHeight(), MapMaker.tileSize*3)
                    );
                }

                tileMap.put(val, image);

                if (indexMap != null) {
                    indexMap.put(val, name);
                }

                if (listModel != null) {
                    listModel.addElement(new ImageIcon(image, val + ""));
                }
            }

            in.close();
        }

        return tileMap;
    }

    public boolean containsTile(TileType tileType, int imageIndex) {
        return tileType.getTileMap(this).containsKey(imageIndex);
    }

    public BufferedImage getTile(TileType tileType, int index) {
        return tileType.getTileMap(this).get(index);
    }

    public void addTile(File imageFile, Color color) {
        color = DrawUtils.permuteColor(color, indexMap);
        BufferedImage img = FileIO.readImage(imageFile);
        mapTileMap.put(color.getRGB(), img);
        indexMap.put(color.getRGB(), imageFile.getName());

        tileListModel.addElement(new ImageIcon(img, color.getRGB() + ""));
        saved = false;
    }

    public void save(MapMaker mapMaker) {
        if (saved) {
            return;
        }

        saved = true;

        final StringBuilder indexFile = new StringBuilder();
        for (final Entry<Integer, String> entry : indexMap.entrySet()) {
            final String imageIndex = Integer.toString(entry.getKey(), 16);
            final String imageName = entry.getValue();

            indexFile.append(imageName)
                    .append(" ")
                    .append(imageIndex)
                    .append("\n");
        }

        FileIO.writeToFile(mapMaker.getPathWithRoot(FileName.MAP_TILES_INDEX), indexFile);
    }
}
