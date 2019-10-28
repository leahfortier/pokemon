package draw.button;

import map.Direction;
import util.Point;

import java.util.Arrays;

public class ButtonTransitions {
    public static final int NO_TRANSITION = -1;

    private final int[] transitions;

    public ButtonTransitions() {
        this.transitions = new int[Direction.values().length];
        Arrays.fill(this.transitions, NO_TRANSITION);
    }

    private boolean hasTransition(Direction direction) {
        return this.getTransition(direction) != NO_TRANSITION;
    }

    private int getTransition(Direction direction) {
        return this.transitions[direction.ordinal()];
    }

    public ButtonTransitions with(Direction direction, int buttonIndex) {
        this.transitions[direction.ordinal()] = buttonIndex;
        return this;
    }

    public ButtonTransitions up(int buttonIndex) {
        return this.with(Direction.UP, buttonIndex);
    }

    public ButtonTransitions down(int buttonIndex) {
        return this.with(Direction.DOWN, buttonIndex);
    }

    public ButtonTransitions left(int buttonIndex) {
        return this.with(Direction.LEFT, buttonIndex);
    }

    public ButtonTransitions right(int buttonIndex) {
        return this.with(Direction.RIGHT, buttonIndex);
    }

    public ButtonTransitions basic(Direction direction, int currentIndex, int numRows, int numCols) {
        return this.basic(direction, currentIndex, numRows, numCols, 0);
    }

    public ButtonTransitions basic(Direction direction, int currentIndex, int numRows, int numCols, int startIndex) {
        return this.with(direction, startIndex + basicTransition(currentIndex, numRows, numCols, direction));
    }

    int[] getTransitions() {
        return this.transitions;
    }

    // Works for all grid buttons
    public static ButtonTransitions getBasicTransitions(int currentIndex, int numRows, int numCols) {
        ButtonTransitions transitions = new ButtonTransitions();
        for (Direction direction : Direction.values()) {
            transitions.basic(direction, currentIndex, numRows, numCols);
        }

        return transitions;
    }

    // Works for all grid buttons
    public static ButtonTransitions getBasicTransitions(int currentIndex, int numRows, int numCols, int startIndex, ButtonTransitions defaultTransitions) {
        // Get the corresponding grid index
        Point location = Point.getPointAtIndex(currentIndex, numCols);

        ButtonTransitions transitions = new ButtonTransitions();
        for (Direction direction : Direction.values()) {
            Point newLocation = Point.add(location, direction.getDeltaPoint());
            boolean inBounds = newLocation.inBounds(numCols, numRows);

            // Default value specified and out of bounds -- use default value instead of wrapping
            if (defaultTransitions != null
                    && defaultTransitions.hasTransition(direction)
                    && !inBounds) {
                transitions.with(direction, defaultTransitions.getTransition(direction));
            } else {
                transitions.basic(direction, currentIndex, numRows, numCols, startIndex);
            }
        }

        return transitions;
    }

    private static int basicTransition(int currentIndex, int numRows, int numCols, Direction direction) {
        // Get the corresponding grid index
        Point location = Point.getPointAtIndex(currentIndex, numCols);

        // Move in the given direction
        location = Point.move(location, direction);

        // Keep in bounds of the grid
        location = Point.modInBounds(location, numRows, numCols);

        // Convert back to single dimension index
        return location.getIndex(numCols);
    }
}
