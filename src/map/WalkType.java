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
}
