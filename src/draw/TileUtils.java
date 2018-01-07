package draw;

import main.Global;
import util.FontMetrics;
import util.Point;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public final class TileUtils {
    // Dimension of a single tile
    private static final Dimension SINGLE_TILE_DIMENSION = new Dimension(1, 1);

    public static Point getLocation(Point drawLocation, Point mapLocation) {
        return new Point(
                (drawLocation.x - mapLocation.x)/Global.TILE_SIZE,
                (drawLocation.y - mapLocation.y)/Global.TILE_SIZE
        );
    }

    public static Point getDrawLocation(int x, int y, Point mapLocation) {
        return new Point(
                x*Global.TILE_SIZE + mapLocation.x,
                y*Global.TILE_SIZE + mapLocation.y
        );
    }

    // Takes in the draw coordinates and returns the location of the entity where to draw it relative to the canvas
    public static Point getDrawLocation(Point location, Point mapLocation) {
        return getDrawLocation(location.x, location.y, mapLocation);
    }

    public static void drawTileImage(Graphics g, BufferedImage image, Point drawLocation) {
        Point imageDrawLocation = Point.add(
                drawLocation,
                Global.TILE_SIZE - image.getWidth(),
                Global.TILE_SIZE - image.getHeight()
        );

        g.drawImage(image, imageDrawLocation.x, imageDrawLocation.y, null);
    }

    public static void drawGrassTile(Graphics g, BufferedImage image, Point drawLocation) {
        Point imageDrawLocation = Point.add(
                drawLocation,
                Global.TILE_SIZE - image.getWidth(),
                Global.TILE_SIZE - 2*image.getHeight()/3
        );

        g.drawImage(image, imageDrawLocation.x, imageDrawLocation.y, null);
    }

    public static void drawGrassTile(Graphics g, BufferedImage image, int x, int y, Point mapLocation) {
        drawGrassTile(g, image, getDrawLocation(x, y, mapLocation));
    }

    public static void drawTileImage(Graphics g, BufferedImage image, int x, int y, Point mapLocation) {
        drawTileImage(g, image, getDrawLocation(x, y, mapLocation));
    }

    public static void drawTileImage(Graphics g, BufferedImage image, Point location, Point mapLocation) {
        drawTileImage(g, image, location.x, location.y, mapLocation);
    }

    public static void outlineTileRed(Graphics g, Point location, Point mapLocation) {
        outlineTile(g, location, mapLocation, Color.RED);
    }

    public static void outlineTile(Graphics g, Point location, Point mapLocation, Color color) {
        outlineTiles(g, location, mapLocation, color, SINGLE_TILE_DIMENSION);
    }

    public static void outlineTiles(Graphics g, Point location, Point mapLocation, Color color, Dimension rectangle) {
        Point drawLocation = getDrawLocation(location, mapLocation);

        g.setColor(color);
        g.drawRect(drawLocation.x, drawLocation.y, Global.TILE_SIZE*rectangle.width, Global.TILE_SIZE*rectangle.height);
    }

    public static void fillTile(Graphics g, Point location, Point mapLocation, Color color) {
        Point drawLocation = getDrawLocation(location, mapLocation);

        g.setColor(color);
        g.fillRect(drawLocation.x, drawLocation.y, Global.TILE_SIZE, Global.TILE_SIZE);
    }

    public static void fillBlankTile(Graphics g, Point drawLocation) {
        int halfTile = Global.TILE_SIZE/2;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                g.setColor(i == j ? Color.GRAY : Color.LIGHT_GRAY);
                g.fillRect(drawLocation.x + i*halfTile, drawLocation.y + j*halfTile, halfTile, halfTile);
            }
        }
    }

    public static BufferedImage createBlankTile() {
        BufferedImage image = createNewTileImage();
        Graphics g = image.getGraphics();
        fillBlankTile(g, new Point());
        g.dispose();

        return image;
    }

    public static BufferedImage createNewTileImage() {
        return new BufferedImage(Global.TILE_SIZE, Global.TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
    }

    public static BufferedImage fillImage(Color color) {
        BufferedImage image = createNewTileImage();
        fillImage(image, color);

        return image;
    }

    public static BufferedImage blankImageWithText(String text) {
        int extra = TextUtils.getTextWidth(text + " ", 14) + 3;

        BufferedImage image = new BufferedImage(Global.TILE_SIZE + extra, Global.TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 14);

        TextUtils.drawCenteredHeightString(g, text, Global.TILE_SIZE + 3, Global.TILE_SIZE/2);

        g.dispose();

        return image;
    }

    public static BufferedImage colorWithText(String text, Color color) {
        BufferedImage image = blankImageWithText(text);
        fillImage(image, color);
        return image;
    }

    public static BufferedImage imageWithText(BufferedImage image, String text) {
        if (image == null) {
            Global.error("Image is null :(");
        }

        BufferedImage bufferedImage = blankImageWithText(text);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bufferedImage;
    }

    public static void fillImage(BufferedImage image, Color color) {
        Graphics g = image.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, Global.TILE_SIZE, Global.TILE_SIZE);
        g.dispose();
    }
}
