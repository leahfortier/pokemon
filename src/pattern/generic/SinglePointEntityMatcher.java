package pattern.generic;

import main.Global;
import util.Point;

public abstract class SinglePointEntityMatcher extends LocationEntityMatcher {
    protected Point location;

    private void setLocation(Point location) {
        this.location = Point.copy(location);
    }

    public Point getLocation() {
        return Point.copy(location);
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
    public void addDelta(Point delta) {
        this.location.add(delta);
    }

    @Override
    public void setLocation(LocationEntityMatcher oldMatcher) {
        if (oldMatcher instanceof SinglePointEntityMatcher) {
            this.setLocation(((SinglePointEntityMatcher) oldMatcher).location);
        } else {
            Global.error("Cannot convert multi point matcher to single point matcher.");
        }
    }
}
