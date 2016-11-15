package item.bag;

import java.io.Serializable;

public enum BattleBagCategory implements Serializable {
    HP_PP("HP/PP", 0x16),
    STATUS("Status", 0x17),
    BALL("Balls", 0x18),
    BATTLE("Battle", 0x19);

    // Name to display in BagView
    private final String name;
    private final int imageNumber;

    private BattleBagCategory(String name, int imgNum) {
        this.name = name;
        this.imageNumber = imgNum;
    }

    public String getName() {
        return name;
    }

    public int getImageNumber() {
        return imageNumber;
    }
}
