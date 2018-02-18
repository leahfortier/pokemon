package draw.button;

import map.Direction;

import java.util.Arrays;

public class ButtonTransitions {
    public static final int NO_TRANSITION = -1;

    private final int[] transitions;

    public ButtonTransitions() {
        this.transitions = new int[Direction.values().length];
        Arrays.fill(this.transitions, NO_TRANSITION);
    }

    public boolean hasTransition(Direction direction) {
        return this.getTransition(direction) != NO_TRANSITION;
    }

    public int getTransition(Direction direction) {
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

    public ButtonTransitions basic(Direction direction, int currentIndex, int numRows, int numCols, int startValue) {
        return this.with(direction, startValue + Button.basicTransition(currentIndex, numRows, numCols, direction));
    }

    int[] getTransitions() {
        return this.transitions;
    }
}
