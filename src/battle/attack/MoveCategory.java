package battle.attack;

import java.io.Serializable;

public enum MoveCategory implements Serializable {
    PHYSICAL(0x23),
    SPECIAL(0x24),
    STATUS(0x25);

    private String name;
    private int imageNumber;

    MoveCategory(int imgNum) {
        imageNumber = imgNum;
        // TODO: I think there's a method that does this already and if there isn't then there should be
        name = name().charAt(0) + name().substring(1).toLowerCase();
    }

    public String toString() {
        return name;
    }

    public int getImageNumber() {
        return imageNumber;
    }
}
