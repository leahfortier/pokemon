package pattern.location;

import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.TriggerMatcher;
import util.Point;

public abstract class LocationTriggerMatcher extends TriggerMatcher implements Comparable<LocationTriggerMatcher> {
    public abstract boolean isAtLocation(Point location);
    public abstract void setLocation(LocationTriggerMatcher oldMatcher);
    public abstract void addPoint(Point point);
    public abstract void addDelta(Point delta);
    public abstract TriggerModelType getTriggerModelType();
    public abstract String getBasicName();

    protected abstract Point getFirstLocationPoint();

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
