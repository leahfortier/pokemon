package gui.panel;

import gui.Button;
import gui.ButtonHoverAction;
import map.Direction;
import util.DrawUtils;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;

public class DrawPanel {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    private int borderPercentage;

    private Color backgroundColor;
    private Color borderColor;

    private Direction[] outlineDirections;

    private boolean transparentBackground;

    public DrawPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.borderPercentage = 10;

        this.backgroundColor = Color.WHITE;
        this.borderColor = Color.LIGHT_GRAY;

        this.outlineDirections = new Direction[0];
    }

    public DrawPanel withBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public DrawPanel withBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public DrawPanel withTransparentBackground() {
        return this.withTransparentBackground(null);
    }

    public DrawPanel withTransparentBackground(Color backgroundColor) {
        this.transparentBackground = true;
        return this.withBackgroundColor(backgroundColor);
    }

    public DrawPanel withBorderPercentage(int borderPercentage) {
        this.borderPercentage = borderPercentage;
        return this;
    }

    public DrawPanel withBlackOutline() {
        this.outlineDirections = Direction.values();
        return this;
    }

    public DrawPanel withBlackOutline(Collection<Direction> directions) {
        this.outlineDirections = directions.toArray(new Direction[0]);
        return this;
    }

    private int getBorderSize() {
        if (transparentBackground && backgroundColor == null) {
            return 0;
        }

        return (int)(borderPercentage/100.0*Math.min(width, height));
    }

    // NOTE: This only works for panels where the width is greater than the height
    public void drawTypeColors(Graphics g, Color[] typeColors) {
        int smallWidth = (width - height)/2;
        int largeWidth = width - smallWidth;

        g.translate(x, y);

        // (0, 0) -> (largeWidth, 0) -> (smallWidth, height) -> (0, height) -> (0, 0)
        g.setColor(typeColors[0]);
        g.fillPolygon(new int[] { 0, largeWidth, smallWidth, 0 }, new int[] { 0, 0, height, height }, 4);

        // (largeWidth, 0) -> (smallWidth, height) -> (width, height) -> (width, 0) -> (largeWidth, 0)
        g.setColor(typeColors[1]);
        g.fillPolygon(new int[] { largeWidth, smallWidth, width, width }, new int[] { 0, height, height, 0 }, 4);

        g.translate(-x, -y);
    }

    public void fillBar(Graphics g, Color color, double ratio) {
        fillTransparent(g);

        g.setColor(color);
        g.fillRect(x, y, (int)(ratio*width), height);

        blackOutline(g);
    }

    public void fillTransparent(Graphics g) {
        DrawUtils.fillTransparent(g, x, y, width, height);
    }

    public void blackOutline(Graphics g) {
        DrawUtils.blackOutline(g, x, y, width, height, outlineDirections);
    }

    public void greyOut(Graphics g) {
        DrawUtils.greyOut(g, x, y, width, height);
    }

    public void drawBackground(Graphics g) {
        if (transparentBackground && backgroundColor == null) {
            fillTransparent(g);
        } else {
            g.setColor(backgroundColor);
            g.fillRect(x, y, width, height);

            int borderSize = this.getBorderSize();

            if (transparentBackground) {
                DrawUtils.fillTransparent(g, x + borderSize, y + borderSize, width - 2*borderSize, height - 2*borderSize);
            } else {
                DrawUtils.drawBorder(g, borderColor, x, y, width, height, borderSize);
            }
        }

        blackOutline(g);
    }

    public Button[] getButtons(int buttonWidth, int buttonHeight, int numRows, int numCols) {
        int borderSize = this.getBorderSize();

        int horizontalSpacing = this.width - 2*borderSize - numCols*buttonWidth;
        int verticalSpacing = this.height - 2*borderSize - numRows*buttonHeight;

        int xSpacing = horizontalSpacing/(numCols + 1);
        int ySpacing = verticalSpacing/(numRows + 1);

        Button[] buttons = new Button[numRows*numCols];
        for (int row = 0, index = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++, index++) {
                buttons[index] = new Button(
                        this.x + borderSize + xSpacing*(col + 1) + buttonWidth*col,
                        this.y + borderSize + ySpacing*(row + 1) + buttonHeight*row,
                        buttonWidth,
                        buttonHeight,
                        ButtonHoverAction.BOX,
                        Button.getBasicTransitions(index, numRows, numCols)
                );
            }
        }

        return buttons;
    }

    private int getTextSpace(Graphics g) {
        return this.getBorderSize() + FontMetrics.getDistanceBetweenRows(g)/2;
    }

    public void drawMessage(Graphics g, int fontSize, String text) {
        g.setColor(Color.BLACK);

        FontMetrics.setFont(g, fontSize);
        int textSpace = this.getTextSpace(g);

        int startX = x + textSpace;
        int startY = y + textSpace + FontMetrics.getTextHeight(g);

        int textWidth = width - 2*textSpace;

        DrawUtils.drawWrappedText(g, text, startX, startY, textWidth);
    }

    public void drawLeftLabel(Graphics g, int fontSize, String label) {
        int startX = x + this.getTextSpace(g);
        int centerY = y + height/2;

        FontMetrics.setFont(g, fontSize);
        DrawUtils.drawCenteredHeightString(g, label, startX, centerY);
    }
}
