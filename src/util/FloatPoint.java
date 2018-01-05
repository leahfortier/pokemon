package util;

public class FloatPoint {
    final float x;
    final float y;
    
    public FloatPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public Point getPoint() {
        return new Point(
                (int)x,
                (int)y
        );
    }
    
    public static FloatPoint scale(float x, float y, float factor) {
        return new FloatPoint(
                x*factor,
                y*factor
        );
    }
    
    public static FloatPoint scale(FloatPoint point, float factor) {
        return scale(point.x, point.y, factor);
    }
    
    public static FloatPoint scale(Point point, float factor) {
        return scale(point.x, point.y, factor);
    }
    
    public static FloatPoint subtract(Point first, FloatPoint second) {
        return new FloatPoint(
                first.x - second.x,
                first.y - second.y
        );
    }
}
