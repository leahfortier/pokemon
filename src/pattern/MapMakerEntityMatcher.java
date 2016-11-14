package pattern;

import mapMaker.model.TriggerModel.TriggerModelType;
import util.Point;

import java.util.Arrays;
import java.util.List;

public abstract class MapMakerEntityMatcher extends EntityMatcher {
    public abstract List<Point> getLocation();
    public abstract void addPoint(Point point);
    public abstract TriggerModelType getTriggerModelType();
    public abstract String getBasicName();

    protected static void addPoint(Point point, List<Point> points, int[] location) {
        points.add(Point.copy(point));

        if (location == null) {
            location = new int[0];
        }

        location = Arrays.copyOf(location, location.length + 2);
        location[location.length - 2] = point.x;
        location[location.length - 1] = point.y;
    }
}
