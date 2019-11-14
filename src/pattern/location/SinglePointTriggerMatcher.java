package pattern.location;

import main.Global;
import util.Point;

import java.util.List;

public abstract class SinglePointTriggerMatcher extends LocationTriggerMatcher {
    private Point location;

    private void setLocation(Point location) {
        this.location = location;
    }

    public Point getLocation() {
        return location;
    }

    @Override
    public void setLocation(LocationTriggerMatcher oldMatcher) {
        if (oldMatcher instanceof SinglePointTriggerMatcher) {
            this.setLocation(((SinglePointTriggerMatcher)oldMatcher).location);
        } else {
            Global.error("Cannot convert multi point matcher to single point matcher.");
        }
    }

    @Override
    public void setLocation(List<Point> location) {
        if (location.size() != 1) {
            Global.error("Invalid location: " + location);
        }

        this.setLocation(location.get(0));
    }

    @Override
    public void addPoint(Point point) {
        this.setLocation(point);
    }

    @Override
    public boolean isAtLocation(Point location) {
        return this.location.equals(location);
    }

    @Override
    public List<Point> getAllLocations() {
        return List.of(this.getLocation());
    }
}
