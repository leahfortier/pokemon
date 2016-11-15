package pattern;

import mapMaker.model.TriggerModel.TriggerModelType;
import util.Point;

public abstract class MapMakerEntityMatcher extends EntityMatcher {
    public abstract boolean isAtLocation(Point location);
    public abstract void setLocation(MapMakerEntityMatcher oldMatcher);
    public abstract void addPoint(Point point);
    public abstract void addDelta(Point delta);
    public abstract TriggerModelType getTriggerModelType();
    public abstract String getBasicName();
}
