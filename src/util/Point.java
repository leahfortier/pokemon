package util;

import map.Direction;

import java.awt.Dimension;
import java.io.Serializable;

// Immutable point class
public class Point implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int x;
    public final int y;

    public Point() {
        this(0, 0);
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Point)) {
            return false;
        }

        Point point = (Point)other;
        return this.x == point.x && this.y == point.y;
    }

    public boolean isZero() {
        return this.x == 0 && this.y == 0;
    }

    public boolean inBounds(int width, int height) {
        return inBounds(x, y, width, height);
    }

    public boolean inBounds(Dimension dimension) {
        return inBounds(x, y, dimension);
    }

    public int getIndex(int width) {
        return getIndex(this.x, this.y, width);
    }

    public Dimension maximizeDimension(Dimension previousDimension) {
        return new Dimension(
                GeneralUtils.max(previousDimension.width, x + 1, previousDimension.width - x),
                GeneralUtils.max(previousDimension.height, y + 1, previousDimension.height - y)
        );
    }

    @Override
    public String toString() {
        return this.x + " " + this.y;
    }

    public static boolean inBounds(int x, int y, Dimension dimension) {
        return inBounds(x, y, dimension.width, dimension.height);
    }

    public static boolean inBounds(int x, int y, int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public static Point modInBounds(Point location, int numRows, int numCols) {
        Point addDimension = add(location, new Point(numCols, numRows));
        return new Point(
                addDimension.x%numCols,
                addDimension.y%numRows
        );
    }

    public static Point add(Point point, int dx, int dy) {
        return new Point(
                point.x + dx,
                point.y + dy
        );
    }

    public static Point add(Point first, Point second, Point... additional) {
        Point point = Point.add(first, second.x, second.y);
        if (additional != null) {
            for (Point additionalPoint : additional) {
                point = Point.add(point, additionalPoint);
            }
        }

        return point;
    }

    public static Point move(Point location, Direction direction) {
        return Point.add(location, direction.getDeltaPoint());
    }

    public static Point subtract(Dimension dimension, int x, int y) {
        return new Point(
                dimension.width - x,
                dimension.height - y
        );
    }

    public static Point subtract(Point first, Point second) {
        return new Point(
                first.x - second.x,
                first.y - second.y
        );
    }

    public static Point negate(Point point) {
        return new Point(
                -point.x,
                -point.y
        );
    }

    public static Point scale(Point point, int factor) {
        return new Point(
                point.x*factor,
                point.y*factor
        );
    }

    private static Point scaleDown(int x, int y, int dividingFactor) {
        return new Point(
                x/dividingFactor,
                y/dividingFactor
        );
    }

    public static Point scaleDown(Point point, int dividingFactor) {
        return scaleDown(point.x, point.y, dividingFactor);
    }

    public static Point scaleDown(Dimension dimension, int dividingFactor) {
        return scaleDown(dimension.width, dimension.height, dividingFactor);
    }

    public static Point lowerBound(Point point) {
        return new Point(
                Math.max(point.x, 0),
                Math.max(point.y, 0)
        );
    }

    public static Point upperBound(Point point, Dimension dimension) {
        return new Point(
                (int)Math.min(point.x, dimension.getWidth() - 1),
                (int)Math.min(point.y, dimension.getHeight() - 1)
        );
    }

    public static Point getDeltaDirection(Point first, Point second) {
        Point difference = subtract(first, second);
        return new Point(
                (int)Math.signum(difference.x),
                (int)Math.signum(difference.y)
        );
    }

    public static Point min(Point start, Point end) {
        return new Point(
                Math.min(start.x, end.x),
                Math.min(start.y, end.y)
        );
    }

    public static Point max(Point start, Point end) {
        return new Point(
                Math.max(start.x, end.x),
                Math.max(start.y, end.y)
        );
    }

    public static int distance(Point start, Point end) {
        Point difference = Point.subtract(start, end);
        return Math.abs(difference.x) + Math.abs(difference.y);
    }

    public static int getIndex(int x, int y, int width) {
        return x + y*width;
    }

    public static Point getPointAtIndex(int locationIndex, int mapWidth) {
        int y = locationIndex/mapWidth;
        int x = locationIndex - y*mapWidth;

        return new Point(x, y);
    }
}
