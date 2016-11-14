package pattern;

import util.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MultiPointEntityMatcher extends MapMakerEntityMatcher {
    public List<Point> location;

    public List<Point> getLocation() {
        if (this.location == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(this.location);
    }

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
}
