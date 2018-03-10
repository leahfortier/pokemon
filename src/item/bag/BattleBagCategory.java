package item.bag;

import util.Serializable;

import java.awt.Color;

public enum BattleBagCategory implements Serializable {
    HP_PP("HP/PP", new Color(245, 144, 146)),
    STATUS("Status", new Color(246, 238, 146)),
    BALL("Balls", new Color(134, 212, 141)),
    BATTLE("Battle", new Color(197, 145, 246));

    // Name to display in BagView
    private final String name;
    private final Color color;

    BattleBagCategory(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return this.name;
    }

    public Color getColor() {
        return this.color;
    }
}
