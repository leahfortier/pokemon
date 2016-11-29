package mapMaker.model;

import map.WalkType;
import mapMaker.MapMaker;
import util.DrawUtils;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

public class MoveModel extends MapMakerModel {
    private final DefaultListModel<ImageIcon> moveListModel;

    MoveModel() {
        super(WalkType.NOT_WALKABLE.getRGB());

        this.moveListModel = new DefaultListModel<>();
    }

    @Override
    public DefaultListModel<ImageIcon> getListModel() {
        return this.moveListModel;
    }

    @Override
    public void reload(MapMaker mapMaker) {
        this.moveListModel.clear();
        for (WalkType type : WalkType.values()) {
            BufferedImage image = DrawUtils.colorWithText(type.name(), type.getColor());
            ImageIcon icon = new ImageIcon(image, type.getRGB() + "");
            this.moveListModel.addElement(icon);
        }
    }

    @Override
    public boolean newTileButtonEnabled() {
        return false;
    }
}
