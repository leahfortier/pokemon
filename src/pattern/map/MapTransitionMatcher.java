package pattern.map;

import map.MapName;
import map.PathDirection;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.SinglePointTriggerMatcher;
import util.Point;

public class MapTransitionMatcher extends SinglePointTriggerMatcher {
    private String exitName;
    private MapName nextMap;
    private String nextEntrance;
    private PathDirection direction;
    private boolean deathPortal;

    private MapName previousMap;

    public MapTransitionMatcher(String exitName, MapName nextMap, String nextEntrance, PathDirection direction, boolean deathPortal) {
        this.exitName = exitName;
        this.nextMap = nextMap;
        this.nextEntrance = nextEntrance;
        this.direction = direction;
        this.deathPortal = deathPortal;
    }

    public void setMapName(final MapName mapName) {
        this.previousMap = mapName;
    }

    public String getExitName() {
        return this.exitName;
    }

    public MapName getNextMap() {
        return this.nextMap;
    }

    public String getNextEntranceName() {
        return this.nextEntrance;
    }

    public PathDirection getDirection() {
        return this.direction;
    }

    public MapName getPreviousMap() {
       return this.previousMap;
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.MAP_TRANSITION;
    }

    @Override
    public String getBasicName() {
        return this.getExitName();
    }

    public Point getExitLocation() {
        if (this.direction == null) {
            return null;
        }

        return Point.add(super.location, direction.getDeltaPoint());
    }

    public boolean isDeathPortal() {
        return deathPortal;
    }
}
