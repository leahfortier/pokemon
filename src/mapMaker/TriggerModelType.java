package mapMaker;

import mapMaker.TileMap.TileType;
import util.DrawMetrics;

import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

public enum TriggerModelType {
    ITEM("Item", TileType.TRAINER, 0x0),
    NPC("NPC", TileType.TRAINER, 0x00000040),
    TRIGGER_ENTITY("Trigger Entity", TileType.MAP_MAKER, 0x4),
    WILD_BATTLE("Wild Battle", TileType.MAP_MAKER, 0x3),
    MAP_EXIT("Map Exit", TileType.MAP_MAKER, 0x2),
    MAP_ENTRANCE("Map Entrance", TileType.MAP_MAKER, 0x1),
    POKE_CENTER("Poke Center", TileType.MAP_MAKER, 0x5),
    TRANSITION_BUILDING("Transition Building", TileType.MAP_MAKER, 0x6),
    EVENT("Event", TileType.MAP_MAKER, 0xc);

    private final String name;
    private final TileType tileType;
    private final int imageIndex;

    TriggerModelType(String name, TileType tileType, int imageIndex) {
        this.name = name;
        this.tileType = tileType;
        this.imageIndex = imageIndex;
    }

    public BufferedImage getImage(final MapMaker mapMaker) {
        return DrawMetrics.imageWithText(mapMaker.getTileFromSet(tileType, imageIndex), name);
    }

    public ImageIcon getImageIcon(final MapMaker mapMaker) {
        return new ImageIcon(this.getImage(mapMaker), this.ordinal() + "");
    }

    public static TriggerModelType getModelTypeFromIndex(int selectedIndex) {
        return TriggerModelType.values()[selectedIndex];
    }
}
