package map;

public enum WalkType {
    WATER(0x0000FF),
    WALKABLE(0xFFFFFF),
    NOT_WALKABLE(0x000000),
    HOP_DOWN(0x00FF00),
    HOP_UP(0xFF0000),
    HOP_LEFT(0xFFFF00),
    HOP_RIGHT(0x00FFFF),
    STAIRS_UP_RIGHT(0xFF00FF),
    STAIRS_UP_LEFT(0xFFC800);

    private final int value;

    WalkType(int value) {
        this.value = value;
    }

    int getValue() {
        return value;
    }
}
