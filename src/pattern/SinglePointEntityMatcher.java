package pattern;

import util.Point;

public abstract class SinglePointEntityMatcher extends MapMakerEntityMatcher {
    protected Point location;

    public Point getLocation() {
        return Point.copy(location);
    }

    public void setPoint(Point point) {
        this.location = Point.copy(point);
    }

    @Override
    public boolean isAtLocation(Point location) {
        return this.location.equals(location);
    }

    @Override
    public void addDelta(Point delta) {
        this.location.add(delta);
    }
}
