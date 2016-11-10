package mapMaker.tools;

import util.Point;
import mapMaker.MapMaker;

public class MoveTool extends Tool {
    private Point previousLocation;

    public MoveTool(MapMaker mapMaker) {
        super(mapMaker);
        previousLocation = new Point(0, 0);
    }

    public void drag(int x, int y) {
        Point dragLocation = new Point(x, y);

        Point difference = Point.subtract(previousLocation, dragLocation);
        mapMaker.getMapLocation().subtract(difference);

        previousLocation = dragLocation;
    }

    public String toString() {
        return "Move";
    }

    public void pressed(int x, int y) {
        previousLocation = new Point(x, y);
    }

    public void reset() {
        previousLocation = mapMaker.getMouseHoverLocation();
    }
}
