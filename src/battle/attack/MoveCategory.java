package battle.attack;

import util.FileIO;
import util.Folder;
import util.StringUtils;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public enum MoveCategory implements Serializable {
    PHYSICAL,
    SPECIAL,
    STATUS;

    private BufferedImage image;

    MoveCategory() {
        String imageName = "MoveCategory" + this.getName();
        this.image = FileIO.readImage(Folder.ATTACK_TILES + imageName);
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public String getName() {
        return StringUtils.properCase(this.name().toLowerCase());
    }
}
