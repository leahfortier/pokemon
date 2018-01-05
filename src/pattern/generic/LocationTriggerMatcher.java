package pattern.generic;

import mapMaker.model.TriggerModel.TriggerModelType;
import util.Point;

import java.util.Comparator;

public abstract class LocationTriggerMatcher extends TriggerMatcher {

    // Compare first by basic name, then by location
    public static final Comparator<LocationTriggerMatcher> COMPARATOR = (first, second) -> {
        int nameCompare = first.getBasicName().compareTo(second.getBasicName());
        if (nameCompare != 0) {
            return nameCompare;
        }
        
        Point firstLocation = first.getFirstLocationPoint();
        Point secondLocation = second.getFirstLocationPoint();
        
        int xCompare = firstLocation.x - secondLocation.x;
        if (xCompare != 0) {
            return xCompare;
        }
        
        return firstLocation.y - secondLocation.y;
    };
    
    public abstract boolean isAtLocation(Point location);
    public abstract void setLocation(LocationTriggerMatcher oldMatcher);
    public abstract void addPoint(Point point);
    public abstract void addDelta(Point delta);
    public abstract TriggerModelType getTriggerModelType();
    public abstract String getBasicName();
    
    protected abstract Point getFirstLocationPoint();
}
