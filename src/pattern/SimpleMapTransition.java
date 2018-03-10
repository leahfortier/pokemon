package pattern;

import map.MapName;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.MultiPointTriggerMatcher;
import util.Serializable;

public class SimpleMapTransition extends MultiPointTriggerMatcher implements Serializable {
    private MapName nextMap;
    private String nextEntrance;

    private transient int transitionIndex;

    public SimpleMapTransition(MapName nextMap, String nextEntrance) {
        this.nextMap = nextMap;
        this.nextEntrance = nextEntrance;
    }

    public int numExits() {
        return nextMap == null || super.location == null ? 0 : super.location.size();
    }

    public int getTransitionIndex() {
        return this.transitionIndex;
    }

    protected void setTransitionIndex(int transitionIndex) {
        this.transitionIndex = transitionIndex;
    }

    public MapName getNextMap() {
        return this.nextMap;
    }

    public String getNextEntranceName() {
        return this.nextEntrance;
    }

    @Override
    public String getBasicName() {
        return null;
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.MAP_TRANSITION;
    }
}
