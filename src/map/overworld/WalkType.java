package map.overworld;

import item.ItemNamesies;
import main.Game;
import map.Direction;
import map.PathDirection;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public enum WalkType {
    NOT_WALKABLE(0x000000, false),
    WALKABLE(0xFFFFFF, true),
    // TODO: Might break NPCs walking from land to water...
    WATER(0x0000FF, direction -> Game.getPlayer().getBag().hasItem(ItemNamesies.SURFBOARD)),
    DOWN_LEDGE(0x00FF00, Direction.DOWN),
    UP_LEDGE(0xFF0000, Direction.UP),
    LEFT_LEDGE(0xFFFF00, Direction.LEFT),
    RIGHT_LEDGE(0x00FFFF, Direction.RIGHT),
    STAIRS_UP_RIGHT(0xFF00FF, true),
    STAIRS_UP_LEFT(0xFFC800, true);

    private static final Map<Integer, WalkType> valueMap = new HashMap<Integer, WalkType>() {{
        for (WalkType walkType : WalkType.values()) {
            this.put(walkType.getRGB(), walkType);
        }
    }};

    private final int rgb;
    private final Predicate<Direction> passableChecker;

    WalkType(int rgb, boolean passable) {
        this(rgb, direction -> passable);
    }

    WalkType(int rgb, Direction passDirection) {
        this(rgb, direction -> direction == passDirection);
    }

    WalkType(int rgb, Predicate<Direction> passableChecker) {
        this.rgb = rgb;
        this.passableChecker = passableChecker;
    }

    public int getRGB() {
        return rgb;
    }

    public Color getColor() {
        return new Color(rgb);
    }

    public boolean isPassable(Direction direction) {
        return this.passableChecker.test(direction);
    }

    // TODO: I don't think the stairs are even being used in the move map -- figure out how to do this better
    public static PathDirection getAdditionalMove(WalkType prev, WalkType next, Direction direction) {
        if (direction == Direction.LEFT) {
            if (next == WalkType.STAIRS_UP_LEFT) {
                return PathDirection.UP;
            } else if (next == WalkType.STAIRS_UP_RIGHT) {
                return PathDirection.DOWN;
            }
        }

        if (direction == Direction.RIGHT) {
            if (prev == WalkType.STAIRS_UP_LEFT) {
                return PathDirection.DOWN;
            } else if (prev == WalkType.STAIRS_UP_RIGHT) {
                return PathDirection.UP;
            }
        }

        return PathDirection.WAIT;
    }

    public static WalkType getWalkType(int value) {
        value &= (1 << 24) - 1;
        if (valueMap.containsKey(value)) {
            return valueMap.get(value);
        }

        return NOT_WALKABLE;
    }
}
