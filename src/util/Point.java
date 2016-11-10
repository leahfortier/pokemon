package util;

import java.awt.Dimension;

public class Point {
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Point)) {
            return false;
        }

        Point p = (Point) o;
        return p.x == x && p.y == y;
    }

    public boolean inBounds(Dimension dimension) {
        return x >= 0 && x < dimension.width && y >= 0 && y < dimension.height;
    }

    public Point add(Point other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public static Point add(Point first, Point second) {
        return copy(first).add(second);
    }

    public Point lowerBound() {
        this.x = Math.max(this.x, 0);
        this.y = Math.max(this.y, 0);
        return this;
    }

    public Point upperBound(Dimension dimension) {
        this.x = (int) Math.min(this.x, dimension.getWidth() - 1);
        this.y = (int) Math.min(this.y, dimension.getHeight() - 1);
        return this;
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

    public int getIndex(int width) {
        return getIndex(this.x, this.y, width);
    }

    public static int getIndex(int x, int y, int width) {
        return x + y * width;
    }

    public static Point getPointAtIndex(int locationIndex, int mapWidth) {
        int y = locationIndex / mapWidth;
        int x = locationIndex - y * mapWidth;

        return new Point(x, y);
    }

    public Point subtract(Point point) {
        this.x -= point.x;
        this.y -= point.y;
        return this;
    }

    public static Point subtract(Point first, Point second) {
        return copy(first).subtract(second);
    }

    public static Point copy(Point point) {
        return new Point(point.x, point.y);
    }

    public Point negate() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public static Point negate(Point point) {
        return copy(point).negate();
    }

    public Dimension maximizeDimension(Dimension previousDimension) {
        return new Dimension(
                max(previousDimension.width, x + 1, previousDimension.width - x),
                max(previousDimension.height, y + 1, previousDimension.height - y)
        );
    }

    public static int max(double... values) {
        double max = values[0];
        for (double value : values) {
            if (value > max) {
                max = value;
            }
        }

        return (int)max;
    }

    public String toString() {
        return this.x + " " + this.y;
    }
}
