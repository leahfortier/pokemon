package pattern.generic;

import mapMaker.model.TriggerModel.TriggerModelType;
import util.Point;

public abstract class LocationEntityMatcher extends EntityMatcher {
    public abstract boolean isAtLocation(Point location);
    public abstract void setLocation(LocationEntityMatcher oldMatcher);
    public abstract void addPoint(Point point);
    public abstract void addDelta(Point delta);
    public abstract TriggerModelType getTriggerModelType();
    public abstract String getBasicName();
}
