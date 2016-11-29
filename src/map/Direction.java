package map;

import input.ControlKey;
import input.InputControl;
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

    Direction(ControlKey key, PathDirection pathDirection) {
        this.key = key;
        this.pathDirection = pathDirection;
    }

    public Point getDeltaPoint() {
        return this.getPathDirection().getDeltaPoint();
    }

    public PathDirection getPathDirection() {
        return this.pathDirection;
    }

    public Direction getOpposite() {
        // TODO: util method for something like this
        return Direction.values()[(this.ordinal() + 2)%Direction.values().length];
    }

    public static Direction getDirection(PathDirection pathDirection) {
        return pathDirectionMap.get(pathDirection);
    }

    // If any of the direction input keys are being pressed, willl return the corresponding direction
    // This method does NOT consume the press
    public static Direction checkInputDirection() {
        return getInputDirection(false);
    }

    // If any of the direction input keys are being pressed, willl return the corresponding direction
    // This method will consume the press if found
    public static Direction consumeInputDirection() {
        return getInputDirection(true);
    }

    private static Direction getInputDirection(boolean consume) {
        InputControl input = InputControl.instance();
        for (Direction direction : Direction.values()) {
            if (input.isDown(direction.key, consume)) {
                return direction;
            }
        }

        return null;
    }
}
