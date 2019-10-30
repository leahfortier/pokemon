package battle.attack;

import type.Type;
import util.file.FileIO;
import util.file.Folder;
import util.string.StringUtils;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

public enum MoveCategory {
    PHYSICAL,
    SPECIAL,
    STATUS;

    public static final Dimension IMAGE_SIZE = Type.IMAGE_SIZE;

    private final BufferedImage image;

    MoveCategory() {
        String imageName = this.getImageName();
        this.image = FileIO.readImage(Folder.ATTACK_TILES + imageName);
    }

    public String getImageName() {
        return "MoveCategory" + StringUtils.properCase(this.name().toLowerCase());
    }

    public BufferedImage getImage() {
        return this.image;
    }
}
