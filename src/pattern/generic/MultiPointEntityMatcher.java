package pattern.generic;

import main.Global;
import util.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MultiPointEntityMatcher extends LocationEntityMatcher {
    public List<Point> location;

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
        return this.location.contains(location);
    }

    @Override
    public void addDelta(Point delta) {
        this.location.forEach(point -> point.add(delta));
    }

    @Override
    public void setLocation(LocationEntityMatcher oldMatcher) {
        if (oldMatcher instanceof MultiPointEntityMatcher) {
            this.location = ((MultiPointEntityMatcher) oldMatcher).getLocation();
        } else {
            Global.error("Cannot convert single point matcher to multi point matcher.");
        }
    }
}
