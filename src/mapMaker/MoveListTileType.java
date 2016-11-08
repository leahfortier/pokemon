package mapMaker;

import javax.swing.ImageIcon;
import java.awt.Color;

/**
 * Created by Leah on 11/8/2016.
 */
enum MoveListTileType {
    IMMOVABLE("Immovable", Color.BLACK),
    MOVABLE("Movable", Color.WHITE),
    WATER("Water", Color.BLUE),
    RIGHT_LEDGE("Right Ledge", Color.CYAN),
    DOWN_LEDGE("Down Ledge", Color.GREEN),
    LEFT_LEDGE("Left Ledge", Color.YELLOW),
    UP_LEDGE("Up Ledge", Color.RED),
    STAIRS_UP_RIGHT("Stairs Up Right", Color.MAGENTA),
    STAIRS_UP_LEFT("Stairs Up Left", Color.ORANGE);

    private String text;
    private Color color;

    MoveListTileType(String text, Color color) {
        this.text = text;
        this.color = color;
    }

    public ImageIcon getImageIcon(MapMaker mapMaker) {
        return new ImageIcon(mapMaker.textWithColor(this.text, this.color), this.color.getRGB() + "");
    }
}
