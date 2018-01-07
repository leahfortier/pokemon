package draw;

import map.Direction;
import util.GeneralUtils;

import java.awt.Color;
import java.awt.Graphics;

public final class PolygonUtils {

    public static void drawCenteredArrow(Graphics g, int centerX, int centerY, int width, int height, Direction direction) {
        drawArrow(g, centerX - width/2, centerY - height/2, width, height, direction);
    }

    public static void drawArrow(Graphics g, int x, int y, int width, int height, Direction direction) {
        int yMax = height;
        int xMax = width;

        boolean yAxis = direction.getDeltaPoint().x == 0;
        if (yAxis) {
            yMax = width;
            xMax = height;
        }

        int arrowLineTop = yMax/4;
        int arrowLineBottom = yMax - arrowLineTop;

        int arrowMidpoint = xMax/2;

        g.translate(x, y);

        int[] xValues = new int[] { 0, arrowMidpoint, arrowMidpoint, xMax, xMax, arrowMidpoint, arrowMidpoint };
        int[] yValues = new int[] { yMax/2, 0, arrowLineTop, arrowLineTop, arrowLineBottom, arrowLineBottom, yMax };

        if (yAxis) {
            GeneralUtils.swapArrays(xValues, yValues);
        }

        if (direction == Direction.RIGHT) {
            for (int i = 0; i < xValues.length; i++) {
                xValues[i] = xMax - xValues[i];
            }
        } else if (direction == Direction.DOWN) {
            for (int i = 0; i < yValues.length; i++) {
                yValues[i] = yMax - yValues[i];
            }
        }

        g.setColor(Color.BLACK);
        g.fillPolygon(xValues, yValues, xValues.length);

        g.translate(-x, -y);
    }

    public static void drawDualColoredBackground(
            Graphics g,
            int x,
            int y,
            int width,
            int height,
            Color firstColor,
            Color secondColor,
            boolean swapDimensions) {
        g.setColor(firstColor);
        g.fillRect(x, y, width, height);

        // Don't need to draw a polygon that is the same color
        if (firstColor.equals(secondColor)) {
            return;
        }

        int smallDimension = Math.min(width, height);
        int largeDimension = Math.max(width, height);

        int smallLength = smallDimension/3;

        if (swapDimensions) {
            int temp = smallDimension;
            smallDimension = largeDimension;
            largeDimension = temp;

            smallLength = largeDimension/3;
        }

        int largeLength = largeDimension - smallLength;

        // (width, 0) -> (large, 0) -> (small, height) -> (width, height)
        int[] rightXValues = new int[] { largeDimension, largeLength, smallLength, largeDimension };
        int[] rightYValues = new int[] { 0, 0, smallDimension, smallDimension };

        if (width < height || swapDimensions) {
            GeneralUtils.swapArrays(rightXValues, rightYValues);
        }

        g.translate(x, y);

        g.setColor(secondColor);
        g.fillPolygon(rightXValues, rightYValues, rightXValues.length);

        g.translate(-x, -y);
    }
}
