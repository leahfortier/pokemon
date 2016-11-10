package mapMaker.tools;

import mapMaker.MapMaker;
import util.Point;

import java.awt.Graphics;

public abstract class Tool {
    final MapMaker mapMaker;

    Tool(final MapMaker mapMaker) {
        this.mapMaker = mapMaker;
    }

    // Can be overridden as necessary by subclasses
    public void click(Point clickLocation) {}
    public void released(Point releasedLocation) {}
    public void pressed(Point pressedLocation) {}
    public void drag(Point dragLocation) {}
    public void draw(Graphics g) {}
    public void reset() {}
}
