package pattern.map;

import map.MapName;
import map.PathDirection;
import pattern.SimpleMapTransition;
import util.Point;

import java.util.List;
import java.util.stream.Collectors;

public class MapTransitionMatcher extends SimpleMapTransition {
    private String exitName;
    private PathDirection direction;
    private boolean deathPortal;

    private MapName previousMap;

    public MapTransitionMatcher(String exitName, MapName nextMap, String nextEntrance, PathDirection direction, boolean deathPortal) {
        super(nextMap, nextEntrance);

        this.exitName = exitName;
        this.direction = direction;
        this.deathPortal = deathPortal;
    }

    public void setMapName(final MapName mapName) {
        this.previousMap = mapName;
    }

    public String getExitName() {
        return this.exitName;
    }

    public PathDirection getDirection() {
        return this.direction;
    }

    public MapName getPreviousMap() {
        return this.previousMap;
    }

    @Override
    public String getBasicName() {
        return this.getExitName();
    }

    public List<Point> getExitLocation() {
        if (this.direction == null) {
            return null;
        }

        return super.location.stream()
                .map(point -> Point.add(point, direction.getDeltaPoint()))
                .collect(Collectors.toList());
    }

    public boolean isDeathPortal() {
        return deathPortal;
    }
}
