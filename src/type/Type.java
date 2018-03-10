package type;

import main.Global;
import util.FileIO;
import util.Folder;
import util.Serializable;

import java.awt.Color;
import java.awt.image.BufferedImage;

public enum Type implements Serializable {
    NORMAL(0, "Normal", () -> TypeAdvantage.NORMAL, new Color(179, 170, 151), -1),
    FIRE(1, "Fire", () -> TypeAdvantage.FIRE, new Color(250, 81, 37), 8),
    WATER(2, "Water", () -> TypeAdvantage.WATER, new Color(48, 158, 255), 9),
    ELECTRIC(3, "Electric", () -> TypeAdvantage.ELECTRIC, new Color(255, 199, 8), 11),
    GRASS(4, "Grass", () -> TypeAdvantage.GRASS, new Color(123, 213, 74), 10),
    ICE(5, "Ice", () -> TypeAdvantage.ICE, new Color(84, 206, 233), 13),
    FIGHTING(6, "Fighting", () -> TypeAdvantage.FIGHTING, new Color(167, 82, 53), 0),
    POISON(7, "Poison", () -> TypeAdvantage.POISON, new Color(184, 88, 167), 2),
    GROUND(8, "Ground", () -> TypeAdvantage.GROUND, new Color(212, 180, 81), 3),
    FLYING(9, "Flying", () -> TypeAdvantage.FLYING, new Color(153, 169, 247), 1),
    PSYCHIC(10, "Psychic", () -> TypeAdvantage.PSYCHIC, new Color(255, 113, 166), 12),
    BUG(11, "Bug", () -> TypeAdvantage.BUG, new Color(173, 191, 0), 5),
    ROCK(12, "Rock", () -> TypeAdvantage.ROCK, new Color(190, 166, 84), 4),
    GHOST(13, "Ghost", () -> TypeAdvantage.GHOST, new Color(99, 97, 185), 6),
    DRAGON(14, "Dragon", () -> TypeAdvantage.DRAGON, new Color(123, 94, 232), 14),
    DARK(15, "Dark", () -> TypeAdvantage.DARK, new Color(116, 90, 73), 15),
    STEEL(16, "Steel", () -> TypeAdvantage.STEEL, new Color(173, 173, 199), 7),
    FAIRY(17, "Fairy", () -> TypeAdvantage.FAIRY, new Color(248, 179, 249), -1),
    NO_TYPE(18, "Unknown", () -> TypeAdvantage.NO_TYPE, new Color(255, 255, 255, 0), -1);

    private final int index;
    private final String name;
    private final AdvantageGetter advantageGetter;
    private final Color color;
    private final int hiddenIndex;
    private final BufferedImage image;

    Type(int index, String name, AdvantageGetter advantageGetter, Color color, int hiddenIndex) {
        this.index = index;
        this.name = name;
        this.advantageGetter = advantageGetter;
        this.color = color;
        this.hiddenIndex = hiddenIndex;

        String imageName = "Type" + name;
        this.image = FileIO.readImage(Folder.TYPE_TILES + imageName);
    }

    public int getIndex() {
        return this.index;
    }

    public String getName() {
        return this.name;
    }

    public TypeAdvantage getAdvantage() {
        return this.advantageGetter.getAdvantage();
    }

    public Color getColor() {
        return this.color;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public static Type getHiddenType(int hiddenIndex) {
        for (Type type : values()) {
            if (type.hiddenIndex == hiddenIndex) {
                return type;
            }
        }

        Global.error("Invalid hidden type index " + hiddenIndex);
        return null;
    }

    @FunctionalInterface
    private interface AdvantageGetter {
        TypeAdvantage getAdvantage();
    }
}
