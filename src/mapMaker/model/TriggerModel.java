package mapMaker.model;

import mapMaker.MapMaker;
import mapMaker.model.TileModel.TileType;
import util.DrawUtils;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

public class TriggerModel extends MapMakerModel {

    private DefaultListModel<ImageIcon> triggerListModel;

    public enum TriggerModelType {
        ITEM("Item", TileType.TRAINER, 0x0),
        NPC("NPC", TileType.TRAINER, 0x00000040),
        TRIGGER_ENTITY("Trigger Entity", TileType.MAP_MAKER, 0x4),
        WILD_BATTLE("Wild Battle", TileType.MAP_MAKER, 0x3),
        MAP_EXIT("Map Exit", TileType.MAP_MAKER, 0x2),
        MAP_ENTRANCE("Map Entrance", TileType.MAP_MAKER, 0x1),
        POKE_CENTER("Poke Center", TileType.MAP_MAKER, 0x5),
        TRANSITION_BUILDING("Transition Building", TileType.MAP_MAKER, 0x6),
        EVENT("Event", TileType.MAP_MAKER, 0xc),
        GROUP("Group", TileType.MAP_MAKER, 0xc);

        private final String name;

        private final TileType tileType;
        private final int imageIndex;
        TriggerModelType(String name, TileType tileType, int imageIndex) {
            this.name = name;
            this.tileType = tileType;
            this.imageIndex = imageIndex;
        }

        public String getName() {
            return this.name;
        }

        public BufferedImage getImage(final MapMaker mapMaker) {
            return mapMaker.getTileFromSet(tileType, imageIndex);
        }

        public ImageIcon getImageIcon(final MapMaker mapMaker) {
            return new ImageIcon(DrawUtils.imageWithText(this.getImage(mapMaker), name), this.ordinal() + "");
        }

        public static TriggerModelType getModelTypeFromIndex(int selectedIndex) {
            return TriggerModelType.values()[selectedIndex];
        }

    }

    TriggerModel() {
        super(-1);

        this.triggerListModel = new DefaultListModel<>();
    }

    @Override
    public void reload(MapMaker mapMaker) {
        this.triggerListModel.clear();

        for (TriggerModelType type : TriggerModelType.values()) {
            this.triggerListModel.addElement(type.getImageIcon(mapMaker));
        }
    }

    @Override
    public DefaultListModel<ImageIcon> getListModel() {
        return this.triggerListModel;
    }

    @Override
    public boolean newTileButtonEnabled() {
        return false;
    }
}
