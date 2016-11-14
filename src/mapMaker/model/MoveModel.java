package mapMaker.model;

import mapMaker.MapMaker;
import util.DrawUtils;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import java.awt.Color;

public class MoveModel extends MapMakerModel {
    private final DefaultListModel<ImageIcon> moveListModel;

    public enum MoveModelType {
        IMMOVABLE("Immovable", Color.BLACK),
        MOVABLE("Movable", Color.WHITE),
        WATER("Water", Color.BLUE),
        RIGHT_LEDGE("Right Ledge", Color.CYAN),
        DOWN_LEDGE("Down Ledge", Color.GREEN),
        LEFT_LEDGE("Left Ledge", Color.YELLOW),
        UP_LEDGE("Up Ledge", Color.RED),
        STAIRS_UP_RIGHT("Stairs Up Right", Color.MAGENTA),
        STAIRS_UP_LEFT("Stairs Up Left", Color.ORANGE);

        private final String name;
        private final Color color;

        MoveModelType(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        private ImageIcon getImageIcon() {
            return new ImageIcon(DrawUtils.colorWithText(this.name, this.color), this.getImageIndex() + "");
        }

        public int getImageIndex() {
            return this.color.getRGB();
        }

        public Color getColor() {
            return this.color;
        }
    }

    MoveModel() {
        super(MoveModelType.IMMOVABLE.getImageIndex());

        this.moveListModel = new DefaultListModel<>();
    }

    @Override
    public DefaultListModel<ImageIcon> getListModel() {
        return this.moveListModel;
    }

    @Override
    public void reload(MapMaker mapMaker) {
        this.moveListModel.clear();
        for (MoveModelType type : MoveModelType.values()) {
            this.moveListModel.addElement(type.getImageIcon());
        }
    }

    @Override
    public boolean newTileButtonEnabled() {
        return false;
    }
}
