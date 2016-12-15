package map;

import util.Point;
import util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum PathDirection {
    RIGHT('r', 1, 0),
    UP('u', 0, -1),
    LEFT('l', -1, 0),
    DOWN('d', 0, 1),
    WAIT('w', 0, 0);

    private static final Map<Character, PathDirection> characterDirectionMap = new HashMap<Character, PathDirection>() {{
        for (PathDirection direction : PathDirection.values()) {
            this.put(direction.character, direction);
        }
    }};

    private final char character;
    private final Point deltaPoint;

    PathDirection(char character, int dx, int dy) {
        this.character = character;
        this.deltaPoint = new Point(dx, dy);
    }

    public Direction getDirection() {
        return Direction.getDirection(this);
    }

    public Point getDeltaPoint() {
        return this.deltaPoint;
    }

    public String getTempPath(int steps) {
        return defaultPath() + StringUtils.repeat(this.character + "", steps);
    }

    public char getCharacter() {
        return this.character;
    }

    public static PathDirection getDirection(char directionCharacter) {
        return characterDirectionMap.get(directionCharacter);
    }

    public static String defaultPath() {
        return WAIT.character + "";
    }
}
