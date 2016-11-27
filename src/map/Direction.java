package map;

import input.ControlKey;
import util.Point;

import java.util.EnumMap;
import java.util.Map;

public enum Direction {
    RIGHT(ControlKey.RIGHT, PathDirection.RIGHT),
    UP(ControlKey.UP, PathDirection.UP),
    LEFT(ControlKey.LEFT, PathDirection.LEFT),
    DOWN(ControlKey.DOWN, PathDirection.DOWN);

    private static final Map<PathDirection, Direction> pathDirectionMap = new EnumMap<PathDirection, Direction>(PathDirection.class) {{
        for (Direction direction : Direction.values()) {
            this.put(direction.getPathDirection(), direction);
        }
    }};

    private final ControlKey key;
    private final PathDirection pathDirection;
    public Direction opposite; // Really this should be final but it won't let me include this in the constructor

    Direction(ControlKey key, PathDirection pathDirection) {
        this.key = key;
        this.pathDirection = pathDirection;
    }

    // This is dumb fuck Java
    static {
        RIGHT.opposite = LEFT;
        UP.opposite = DOWN;
        LEFT.opposite = RIGHT;
        DOWN.opposite = UP;
    }

    public Point getDeltaPoint() {
        return this.getPathDirection().getDeltaPoint();
    }

    public ControlKey getKey() {
        return this.key;
    }

    public PathDirection getPathDirection() {
        return this.pathDirection;
    }

    public static Direction getDirection(PathDirection pathDirection) {
        return pathDirectionMap.get(pathDirection);
    }
}
