package mapMaker.model;

import draw.TileUtils;
import mapMaker.MapMaker;
import mapMaker.model.TileModel.TileType;
import mapMaker.tools.Tool.ToolType;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

public class TriggerModel extends MapMakerModel {
    private final DefaultListModel<ImageIcon> triggerListModel;

    public TriggerModel() {
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

    // TODO: I hate everything
    public static BufferedImage getMapExitImage(final MapMaker mapMaker) {
        return mapMaker.getTileFromSet(TileType.MAP_MAKER, 0x2);
    }

    public enum TriggerModelType {
        ITEM("Item", true, 0xf),
        HIDDEN_ITEM("Hidden Item", true, 0x10),
        NPC("NPC", TileType.TRAINER, true, 0x40),
        MISC_ENTITY("Misc Entity", true, 0x4),
        WILD_BATTLE("Wild Battle", false, 0x3),
        MAP_TRANSITION("Map Exit", true, 0x1),
        EVENT("Event", true, 0xc),
        FISHING("Fishing", false, 0xe);

        private final String name;

        private final TileType tileType;
        private final ToolType defaultTool;
        private final int imageIndex;

        TriggerModelType(String name, boolean singleClick, int imageIndex) {
            this(name, TileType.MAP_MAKER, singleClick, imageIndex);
        }

        TriggerModelType(String name, TileType tileType, boolean singleClick, int imageIndex) {
            this.name = name;
            this.tileType = tileType;
            this.defaultTool = singleClick ? ToolType.SINGLE_CLICK : ToolType.RECTANGLE;
            this.imageIndex = imageIndex;
        }

        public String getName() {
            return this.name;
        }

        public BufferedImage getImage(final MapMaker mapMaker) {
            return mapMaker.getTileFromSet(tileType, imageIndex);
        }

        public ImageIcon getImageIcon(final MapMaker mapMaker) {
            return new ImageIcon(TileUtils.tileWithText(this.getImage(mapMaker), name), String.valueOf(this.ordinal()));
        }

        public ToolType getDefaultTool() {
            return this.defaultTool;
        }

        public static TriggerModelType getModelTypeFromIndex(int selectedIndex) {
            return TriggerModelType.values()[selectedIndex];
        }
    }
}
