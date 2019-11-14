package pattern.location;

import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.TriggerMatcher;
import util.Point;

import java.util.List;
import java.util.stream.Collectors;

public abstract class LocationTriggerMatcher extends TriggerMatcher implements Comparable<LocationTriggerMatcher> {
    public abstract boolean isAtLocation(Point location);
    public abstract void setLocation(LocationTriggerMatcher oldMatcher);
    public abstract void setLocation(List<Point> location);
    public abstract void addPoint(Point point);
    public abstract TriggerModelType getTriggerModelType();
    public abstract String getBasicName();
    public abstract List<Point> getAllLocations();

    // Add delta to each point in location and reset location
    public void addDelta(Point delta) {
        this.setLocation(this.getAllLocations()
                            .stream()
                            .map(point -> Point.add(point, delta))
                            .collect(Collectors.toList()));
    }

    private Point getFirstLocationPoint() {
        return this.getAllLocations().get(0);
    }

    // Compare first by basic name, then by location
    @Override
    public int compareTo(LocationTriggerMatcher that) {
        int nameCompare = this.getBasicName().compareTo(that.getBasicName());
        if (nameCompare != 0) {
            return nameCompare;
        }

        Point thisLocation = this.getFirstLocationPoint();
        Point thatLocation = that.getFirstLocationPoint();

        int xCompare = thisLocation.x - thatLocation.x;
        if (xCompare != 0) {
            return xCompare;
        }

        return thisLocation.y - thatLocation.y;
    }
}
