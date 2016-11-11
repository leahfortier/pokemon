package mapMaker.model;

import main.Global;
import mapMaker.MapMaker;
import util.DrawUtils;
import util.FileIO;
import util.FileName;
import util.Folder;
import util.StringUtils;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class TileModel extends MapMakerModel {
    private static final int BLANK_TILE_INDEX = Color.MAGENTA.getRGB(); // TODO: No I hate this
    private static final BufferedImage BLANK_TILE_IMAGE = DrawUtils.createBlankTile();
    private static final ImageIcon BLANK_TILE_ICON = new ImageIcon(BLANK_TILE_IMAGE, "0"); // TODO: Is 0 still necessary?

    private final DefaultListModel<ImageIcon> tileListModel;

    private final Map<Integer, String> indexMap;
    private boolean saved;

    private final Map<Integer, BufferedImage> mapTileMap;
    private final Map<Integer, BufferedImage> mapMakerTileMap;
    private final Map<Integer, BufferedImage> trainerTileMap;
    private final Map<Integer, BufferedImage> itemTileMap;

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
            Map<Integer, BufferedImage> getTileMap(TileModel tileModel);
        }

        private Map<Integer, BufferedImage> getTileMap(TileModel tileModel) {
            return this.tileMapGetter.getTileMap(tileModel);
        }
    }

    public TileModel(MapMaker mapMaker) {
        super(BLANK_TILE_INDEX);

        this.tileListModel = new DefaultListModel<>();

        this.indexMap = new HashMap<>();
        this.saved = true;

        this.mapTileMap = new HashMap<>();
        this.trainerTileMap = new HashMap<>();
        this.mapMakerTileMap = new HashMap<>();
        this.itemTileMap = new HashMap<>();

        this.reload(mapMaker);
    }

    @Override
    public DefaultListModel<ImageIcon> getListModel() {
        return this.tileListModel;
    }

    @Override
    public void reload(MapMaker mapMaker) {
        loadTiles(this.mapTileMap, Folder.MAP_TILES, FileName.MAP_TILES_INDEX, mapMaker, this.tileListModel, this.indexMap, true);
        this.indexMap.put(BLANK_TILE_INDEX, "BlankImage");
        this.tileListModel.add(0, BLANK_TILE_ICON);

        loadTiles(this.trainerTileMap, Folder.TRAINER_TILES, FileName.TRAINER_TILES_INDEX, mapMaker);
        loadTiles(this.mapMakerTileMap, Folder.MAP_MAKER_TILES, FileName.MAP_MAKER_TILES_INDEX, mapMaker);
        loadTiles(this.itemTileMap, Folder.ITEM_TILES, FileName.ITEM_TILES_INDEX, mapMaker);
    }

    @Override
    public boolean newTileButtonEnabled() {
        return true;
    }

    private Map<Integer, BufferedImage> loadTiles(Map<Integer, BufferedImage> tileMap, String tileFolderName, String indexFileName, MapMaker mapMaker) {
        return this.loadTiles(tileMap, tileFolderName, indexFileName, mapMaker, null, null, false);
    }

    private Map<Integer, BufferedImage> loadTiles(
            Map<Integer, BufferedImage> tileMap,
            String tileFolderName,
            String indexFileName,
            MapMaker mapMaker,
            DefaultListModel<ImageIcon> listModel,
            Map<Integer, String> indexMap,
            boolean resize) {
        File indexFile = new File(mapMaker.getPathWithRoot(indexFileName));
        tileMap.clear();

        if (indexMap != null) {
            indexMap.clear();
        }

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
                            Math.min(image.getWidth(), Global.TILE_SIZE*3),
                            Math.min(image.getHeight(), Global.TILE_SIZE*3)
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

    @Override
    public void newTileButtonPressed(MapMaker mapMaker) {

        JFileChooser fileChooser = FileIO.getImageFileChooser(mapMaker.getPathWithRoot(Folder.MAP_TILES));

        int val = fileChooser.showOpenDialog(mapMaker);
        if (val == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File imageFile: files) {
                Color color = JColorChooser.showDialog(mapMaker, "Choose a preferred color for tile: " + imageFile.getName(), Color.WHITE);
                if (color == null) {
                    continue;
                }

                color = DrawUtils.permuteColor(color, indexMap);
                BufferedImage img = FileIO.readImage(imageFile);
                mapTileMap.put(color.getRGB(), img);
                indexMap.put(color.getRGB(), imageFile.getName());

                tileListModel.addElement(new ImageIcon(img, color.getRGB() + ""));
                saved = false;
            }
        }
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

            StringUtils.appendLine(indexFile, imageName + " " + imageIndex);
        }

        FileIO.writeToFile(mapMaker.getPathWithRoot(FileName.MAP_TILES_INDEX), indexFile);
    }
}
