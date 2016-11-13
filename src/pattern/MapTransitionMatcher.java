package pattern;

import map.Direction;
import mapMaker.model.TriggerModel.TriggerModelType;
import util.Point;

import java.util.ArrayList;
import java.util.List;

public class MapTransitionMatcher extends MapMakerEntityMatcher {
    private String exitName;
    private int[] location;
    private String nextMap;
    private String nextEntrance;
    private Direction direction;
    private boolean deathPortal;

    private String previousMap;

    private transient List<Point> entrances;
    private transient List<Point> exits;

    public MapTransitionMatcher(String exitName, String nextMap, String nextEntrance, Direction direction, boolean deathPortal) {
        this.exitName = exitName;
        this.nextMap = nextMap;
        this.nextEntrance = nextEntrance;
        this.direction = direction;
        this.deathPortal = deathPortal;
    }

    public void setMapName(final String mapName) {
        this.previousMap = mapName;
    }

    public String getExitName() {
        return this.exitName;
    }

    public String getNextMap() {
        return this.nextMap;
    }

    public String getNextEntranceName() {
        return this.nextEntrance;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public String getPreviousMap() {
       return this.previousMap;
    }

    public List<Point> getLocation() {
        if (entrances != null) {
            return entrances;
        }

        this.entrances = new ArrayList<>();
        if (this.location != null) {
            for (int i = 0; i < this.location.length; i += 2) {
                int x = this.location[i];
                int y = this.location[i + 1];

                this.entrances.add(new Point(x, y));
            }
        }

        return this.entrances;
    }

    @Override
    public void addPoint(Point point) {
        MapMakerEntityMatcher.addPoint(point, this.getEntrances(), this.location);
        this.addExitPoint(Point.copy(point));
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.MAP_TRANSITION;
    }

    @Override
    public String getBasicName() {
        return this.getExitName();
    }

    public List<Point> getEntrances() {
        return this.getLocation();
    }

    private void addExitPoint(Point entrance) {
        if (this.direction != null) {
            // TODO: dx, dy -> delta
            int exitX = entrance.x + this.direction.dx;
            int exitY = entrance.y + this.direction.dy;

            this.getExits().add(new Point(exitX, exitY));
        }
    }

    public List<Point> getExits() {
        if (this.exits != null) {
            return this.exits;
        }

        this.exits = new ArrayList<>();

        List<Point> entrances = this.getEntrances();
        for (Point entrance : entrances) {
            this.addExitPoint(entrance);
        }

        return this.exits;
    }

    public boolean isDeathPortal() {
        return deathPortal;
    }
}
