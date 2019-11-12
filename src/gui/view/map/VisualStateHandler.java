package gui.view.map;

import main.Game;

import java.awt.Graphics;

abstract class VisualStateHandler {
    protected MapView view;

    public VisualStateHandler() {
        this.view = Game.instance().getMapView();
    }

    public void resetMap(MapView view) {
        this.view = view;
    }

    public abstract void draw(Graphics g);
    public abstract void update(int dt);

    public void set() {}
}
