package item.bag;

import java.awt.Color;
import java.io.Serializable;

public enum BagCategory implements Serializable {
    MEDICINE("Medicine", new Color(248, 120, 64), 0x1),
    BALL("Balls", new Color(232, 184, 40), 0x2),
    STAT("Stat", new Color(80, 128, 232), 0x3),
    KEY_ITEM("KeyItems", new Color(152, 88, 240), 0x4),
    TM("TMs", new Color(168, 232, 72), 0x5),
    BERRY("Berries", new Color(64, 192, 64), 0x6),
    MISC("Misc", new Color(232, 136, 192), 0x7);

    // Name to display in BagView
    private String name;
    private Color color;
    private int imageNumber;

    BagCategory(String name, Color color, int imageNumber) {
        this.name = name;
        this.color = color;
        this.imageNumber = imageNumber;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public int getImageNumber() {
        return imageNumber;
    }
}