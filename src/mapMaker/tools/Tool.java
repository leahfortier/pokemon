package mapMaker.tools;

import mapMaker.MapMaker;

import java.awt.Graphics;

public abstract class Tool {
    final MapMaker mapMaker;

    Tool(final MapMaker mapMaker) {
        this.mapMaker = mapMaker;
    }

    // Can be overridden as necessary by subclasses
    public void click(int x, int y) {}
    public void released(int x, int y) {}
    public void pressed(int x, int y) {}
    public void drag(int x, int y) {}
    public void draw(Graphics g) {}
    public void reset() {}
}
