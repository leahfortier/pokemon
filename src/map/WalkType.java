package map;

public enum WalkType {
    WATER(0x0000FF, false), // TODO
    WALKABLE(0xFFFFFF, true),
    NOT_WALKABLE(0x000000, false),
    HOP_DOWN(0x00FF00, Direction.DOWN),
    HOP_UP(0xFF0000, Direction.UP),
    HOP_LEFT(0xFFFF00, Direction.LEFT),
    HOP_RIGHT(0x00FFFF, Direction.RIGHT),
    STAIRS_UP_RIGHT(0xFF00FF, true),
    STAIRS_UP_LEFT(0xFFC800, true);

    private final int value;
    private final PassableChecker passableChecker;

    WalkType(int value, boolean passable) {
        this(value, direction -> passable);
    }

    WalkType(int value, Direction passDirection) {
        this(value, direction -> direction == passDirection);
    }

    WalkType(int value, PassableChecker passableChecker) {
        this.value = value;
        this.passableChecker = passableChecker;
    }

    int getValue() {
        return value;
    }

    public boolean isPassable(Direction direction) {
        return this.passableChecker.isPassable(direction);
    }

    private interface PassableChecker {
        boolean isPassable(Direction direction);
    }

    // TODO: I don't think the stairs are even being used in the move map -- figure out how to do this better
    public static PathDirection getAdditionalMove(WalkType prev, WalkType next, Direction direction) {
        if (direction == Direction.LEFT) {
            if (next == WalkType.STAIRS_UP_LEFT) {
                return PathDirection.UP;
            }
            else if (next == WalkType.STAIRS_UP_RIGHT) {
                return PathDirection.DOWN;
            }
        }

        if (direction == Direction.RIGHT) {
            if (prev == WalkType.STAIRS_UP_LEFT) {
                return PathDirection.DOWN;
            }
            else if (prev == WalkType.STAIRS_UP_RIGHT) {
                return PathDirection.UP;
            }
        }

        return PathDirection.WAIT;
    }
}
