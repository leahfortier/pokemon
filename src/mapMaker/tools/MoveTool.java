package mapMaker.tools;

import mapMaker.MapMaker;

public class MoveTool extends Tool {
    private int prevX, prevY;

    public MoveTool(MapMaker mapMaker) {
        super(mapMaker);
    }

    public void drag(int x, int y) {
        mapMaker.mapX -= prevX - x;
        mapMaker.mapY -= prevY - y;
        prevX = x;
        prevY = y;
    }

    public String toString() {
        return "Move";
    }

    public void pressed(int x, int y) {
        prevX = x;
        prevY = y;
    }

    public void reset() {
        prevX = mapMaker.mouseHoverX;
        prevY = mapMaker.mouseHoverY;
    }
}
