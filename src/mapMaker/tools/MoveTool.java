package mapMaker.tools;

import util.Point;
import mapMaker.MapMaker;

public class MoveTool extends Tool {
    private Point previousLocation;

    public MoveTool(MapMaker mapMaker) {
        super(mapMaker);
        previousLocation = new Point();
    }

    @Override
    public void drag(Point dragLocation) {
        Point difference = Point.subtract(previousLocation, dragLocation);
        mapMaker.getMapLocation().subtract(difference);

        previousLocation = Point.copy(dragLocation);
    }

    @Override
    public void pressed(Point pressedLocation) {
        previousLocation = Point.copy(pressedLocation);
    }

    @Override
    public void reset() {
        previousLocation = Point.copy(mapMaker.getMouseHoverLocation());
    }

    public String toString() {
        return "Move";
    }
}
