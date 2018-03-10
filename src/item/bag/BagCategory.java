package item.bag;

import util.file.FileIO;
import util.file.Folder;

import java.awt.Color;
import java.awt.image.BufferedImage;

public enum BagCategory {
    MEDICINE("Medicine", new Color(248, 120, 64)),
    BALL("Balls", new Color(232, 184, 40)),
    STAT("Stat", new Color(80, 128, 232)),
    KEY_ITEM("Key Items", new Color(152, 88, 240)),
    TM("TMs", new Color(168, 232, 72)),
    BERRY("Berries", new Color(64, 192, 64)),
    MISC("Misc", new Color(232, 136, 192));

    private final String displayName;
    private final Color color;
    private final BufferedImage icon;

    BagCategory(String displayName, Color color) {
        this.displayName = displayName;
        this.color = color;

        String imageName = "cat_" + this.displayName.replaceAll("\\s", "").toLowerCase();
        this.icon = FileIO.readImage(Folder.BAG_TILES + imageName);
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getColor() {
        return color;
    }

    public BufferedImage getIcon() {
        return icon;
    }
}
