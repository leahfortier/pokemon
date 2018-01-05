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
    
    private final BufferedImage image;
    
    MoveCategory() {
        String imageName = "MoveCategory" + StringUtils.properCase(this.name().toLowerCase());
        this.image = FileIO.readImage(Folder.ATTACK_TILES + imageName);
    }
    
    public BufferedImage getImage() {
        return this.image;
    }
}
