package map.entity.movable;

import map.Direction;
import util.Point;

public enum MoveAxis {
    X_ONLY((thisLocation, thisDirection, otherLocation) -> thisLocation.x == otherLocation.x),
    Y_ONLY((thisLocation, thisDirection, otherLocation) -> thisLocation.y == otherLocation.y),
    BOTH((thisLocation, thisDirection, otherLocation) ->
                 X_ONLY.checker.checkAxis(thisLocation, thisDirection, otherLocation)
                         || Y_ONLY.checker.checkAxis(thisLocation, thisDirection, otherLocation)
    ),
    FACING((thisLocation, thisDirection, otherLocation) -> {
        // Not in the same row or the same column
        if (!BOTH.checker.checkAxis(thisLocation, thisDirection, otherLocation)) {
            return false;
        }

        // Get the direction that would be facing the other location
        Point deltaDirection = Point.getDeltaDirection(otherLocation, thisLocation);

        // Check if these are the same direction
        return thisDirection.getDeltaPoint().equals(deltaDirection);
    });

    private final AxisChecker checker;

    MoveAxis(AxisChecker checker) {
        this.checker = checker;
    }

    public boolean checkAxis(Point thisLocation, Direction thisDirection, Point otherLocation) {
        return this.checker.checkAxis(thisLocation, thisDirection, otherLocation);
    }

    @FunctionalInterface
    private interface AxisChecker {
        boolean checkAxis(Point thisLocation, Direction thisDirection, Point otherLocation);
    }
}
