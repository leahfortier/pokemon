package pattern.generic;

import main.Global;
import util.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MultiPointTriggerMatcher extends LocationTriggerMatcher {
    protected List<Point> location;

    public List<Point> getLocation() {
        if (this.location == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(this.location);
    }

    @Override
    public void addPoint(Point point) {
        if (this.location == null) {
            this.location = new ArrayList<>();
        }

        this.location.add(point);
    }

    @Override
    public boolean isAtLocation(Point location) {
        return this.location != null && this.location.contains(location);
    }

    @Override
    public void addDelta(Point delta) {
        this.location = this.getLocation()
                .stream()
                .map(point -> Point.add(point, delta))
                .collect(Collectors.toList());
    }

    @Override
    public void setLocation(LocationTriggerMatcher oldMatcher) {
        if (oldMatcher instanceof MultiPointTriggerMatcher) {
            this.location = ((MultiPointTriggerMatcher) oldMatcher).getLocation();
        } else {
            Global.error("Cannot convert single point matcher to multi point matcher.");
        }
    }

    @Override
    protected Point getFirstLocationPoint() {
        return this.getLocation().get(0);
    }
}
