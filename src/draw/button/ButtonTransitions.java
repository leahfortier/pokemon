package draw.button;

import map.Direction;

public class ButtonTransitions {
    public static final int NO_TRANSITION = -1;

    private final int[] transitions;

    public ButtonTransitions(int[] transitions) {
        if (transitions == null) {
            this.transitions = new int[] { NO_TRANSITION, NO_TRANSITION, NO_TRANSITION, NO_TRANSITION };
        } else {
            this.transitions = transitions;
        }
    }

    public int next(Direction direction) {
        return this.transitions[direction.ordinal()];
    }
}
