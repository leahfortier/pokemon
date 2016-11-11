package mapMaker.model;

import mapMaker.MapMaker;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;

public abstract class MapMakerModel {
    private final int blankTileIndex;

    protected MapMakerModel(int blankTileIndex) {
        this.blankTileIndex = blankTileIndex;
    }

    public int getBlankTileIndex() {
        return this.blankTileIndex;
    }

    public abstract DefaultListModel<ImageIcon> getListModel();
    public abstract void reload(MapMaker mapMaker);
    public abstract boolean newTileButtonEnabled();

    // Should be overridden by subclasses which return true to newTileButtonEnabled
    public void newTileButtonPressed(MapMaker mapMaker) {}

    // Additional draw actions can be overridden
    public void draw(Graphics2D g, MapMaker mapMaker) {}
}
