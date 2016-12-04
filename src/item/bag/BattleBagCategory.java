package item.bag;

import java.awt.Color;
import java.io.Serializable;

public enum BattleBagCategory implements Serializable {
    HP_PP("HP/PP", new Color(251, 211, 212), new Color(245, 144, 146)),
    STATUS("Status", new Color(251, 248, 211), new Color(246, 238, 146)),
    BALL("Balls", new Color(207, 237, 210), new Color(134, 212, 141)),
    BATTLE("Battle", new Color(232, 212, 251), new Color(197, 145, 246));

    // Name to display in BagView
    private final String name;
    private final Color backgroundColor;
    private final Color borderColor;

    BattleBagCategory(String name, Color backgroundColor, Color borderColor) {
        this.name = name;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
    }

    public String getName() {
        return this.name;
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public Color getBorderColor() {
        return this.borderColor;
    }
}
