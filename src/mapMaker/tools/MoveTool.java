package mapMaker.tools;

import util.Point;
import mapMaker.MapMaker;

class MoveTool extends Tool {
    private Point previousLocation;

    MoveTool(MapMaker mapMaker) {
        super(mapMaker);
        previousLocation = new Point();
    }

    @Override
    public void drag(Point dragLocation) {
        Point difference = Point.subtract(previousLocation, dragLocation);
        mapMaker.offSetLocation(Point.negate(difference));

        previousLocation = dragLocation;
    }

    @Override
    public void pressed(Point pressedLocation) {
        previousLocation = pressedLocation;
    }

    @Override
    public void reset() {
        previousLocation = mapMaker.getMouseHoverLocation();
    }

    public String toString() {
        return "Move";
    }
}
