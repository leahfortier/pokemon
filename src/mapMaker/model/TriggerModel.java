package mapMaker.model;

import draw.TileUtils;
import map.entity.movable.MovableEntity;
import mapMaker.MapMaker;
import mapMaker.model.TileModel.TileType;
import mapMaker.tools.Tool.ToolType;
import pattern.location.LocationTriggerMatcher;
import pattern.map.NPCMatcher;
import pattern.map.WildBattleAreaMatcher;
import util.file.FileIO;
import util.file.Folder;

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
            this.triggerListModel.addElement(type.getImageIcon());
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
    public static BufferedImage getMapExitImage() {
        return FileIO.readImage(Folder.MAP_MAKER_TILES + "ExitTriggerTile");
    }

    public enum TriggerModelType {
        ITEM("Item", true, "Item"),
        HIDDEN_ITEM("Hidden Item", true, "HiddenItem"),
        NPC("NPC", true, Folder.TRAINER_TILES + "Male01StandingDown", (mapMaker, entity) -> {
            NPCMatcher npc = (NPCMatcher)entity;
            int imageIndex = MovableEntity.getTrainerSpriteIndex(npc.getSpriteIndex(), npc.getDirection());
            return mapMaker.getTileFromSet(TileType.TRAINER, imageIndex);
        }),
        MISC_ENTITY("Misc Entity", true, "TriggerEntity"),
        WILD_BATTLE("Wild Battle", false, wildBattleImageName(0), (mapMaker, entity) -> {
            WildBattleAreaMatcher wildBattleArea = (WildBattleAreaMatcher)entity;
            int index = mapMaker.getTriggerData().getWildBattleAreaIndex(wildBattleArea);
            return FileIO.readImage(wildBattleImageName(index));
        }),
        MAP_TRANSITION("Map Exit", true, "EntranceTriggerTile"),
        EVENT("Event", true, "EventTrigger"),
        FISHING("Fishing", false, "FishingTrigger");

        private final String name;

        private final ToolType defaultTool;

        private final String defaultImage;
        private final ImageGetter imageGetter;

        TriggerModelType(String name, boolean singleClick, String defaultImage) {
            this(name, singleClick, Folder.MAP_MAKER_TILES + defaultImage, null);
        }

        TriggerModelType(String name, boolean singleClick, String defaultImagePath, ImageGetter imageGetter) {
            this.name = name;
            this.defaultTool = singleClick ? ToolType.SINGLE_CLICK : ToolType.RECTANGLE;
            this.defaultImage = defaultImagePath;
            this.imageGetter = imageGetter;
        }

        public String getName() {
            return this.name;
        }

        private BufferedImage getDefaultImage() {
            return FileIO.readImage(defaultImage);
        }

        public BufferedImage getImage(MapMaker mapMaker, LocationTriggerMatcher entity) {
            if (imageGetter == null) {
                return this.getDefaultImage();
            }

            return imageGetter.getImageName(mapMaker, entity);
        }

        public ImageIcon getImageIcon() {
            return new ImageIcon(TileUtils.tileWithText(this.getDefaultImage(), name), this.ordinal() + "");
        }

        public ToolType getDefaultTool() {
            return this.defaultTool;
        }

        public static TriggerModelType getModelTypeFromIndex(int selectedIndex) {
            return TriggerModelType.values()[selectedIndex];
        }

        private static String wildBattleImageName(int index) {
            return Folder.MAP_MAKER_TILES + "WildBattleIndicator" + index;
        }

        @FunctionalInterface
        public interface ImageGetter {
            BufferedImage getImageName(MapMaker mapMaker, LocationTriggerMatcher entity);
        }
    }
}
