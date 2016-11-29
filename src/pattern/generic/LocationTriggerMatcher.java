package pattern.generic;

import mapMaker.model.TriggerModel.TriggerModelType;
import util.Point;

public abstract class LocationTriggerMatcher extends TriggerMatcher implements Comparable<LocationTriggerMatcher> {
    public abstract boolean isAtLocation(Point location);
    public abstract void setLocation(LocationTriggerMatcher oldMatcher);
    public abstract void addPoint(Point point);
    public abstract void addDelta(Point delta);
    public abstract TriggerModelType getTriggerModelType();
    public abstract String getBasicName();

    @Override
    public int compareTo(LocationTriggerMatcher other) {
        return this.getTriggerName().compareTo(other.getTriggerName());
    }
}
