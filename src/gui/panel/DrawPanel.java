package gui.panel;

import gui.button.Button;
import gui.button.ButtonHoverAction;
import map.Direction;
import util.DrawUtils;
import util.FontMetrics;
import util.Point;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Collection;

public class DrawPanel {

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    private int borderPercentage;

    private Color backgroundColor;
    private Color borderColor;
    private Color secondBackgroundColor;

    private Direction[] outlineDirections;

    private boolean transparentBackground;
    private boolean greyOut;

    private boolean onlyTransparency;
    private int transparentCount;

    public DrawPanel(int x, int y, Dimension dimension) {
        this(x, y, dimension.width, dimension.height);
    }

    public DrawPanel(int x, int y, Point dimension) {
        this(x, y, dimension.x, dimension.y);
    }

    public DrawPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.borderPercentage = 10;

        this.backgroundColor = Color.WHITE;
        this.borderColor = Color.LIGHT_GRAY;

        this.outlineDirections = new Direction[0];

        this.transparentCount = 1;
    }

    public DrawPanel withBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public DrawPanel withBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public DrawPanel withBackgroundColors(Color[] backgroundColors) {
        this.backgroundColor = backgroundColors[0];
        this.secondBackgroundColor = backgroundColors[1];
        return this;
    }

    public DrawPanel withTransparentCount(int transparentCount) {
        this.transparentCount = transparentCount;
        return this.withTransparentBackground();
    }

    public DrawPanel withFullTransparency() {
        this.onlyTransparency = true;
        return this.withTransparentBackground().withBorderPercentage(0);
    }

    public DrawPanel withTransparentBackground() {
        this.transparentBackground = true;
        return this;
    }

    public DrawPanel withTransparentBackground(Color backgroundColor) {
        return this.withTransparentBackground().withBackgroundColor(backgroundColor);
    }

    public DrawPanel withBorderPercentage(int borderPercentage) {
        this.borderPercentage = borderPercentage;
        return this;
    }

    public DrawPanel withBlackOutline() {
        this.outlineDirections = Direction.values();
        return this;
    }

    public DrawPanel greyOut() {
        this.greyOut = true;
        return this;
    }

    public DrawPanel withBlackOutline(Collection<Direction> directions) {
        this.outlineDirections = directions.toArray(new Direction[0]);
        return this;
    }

    public int getBorderSize() {
        if (onlyTransparency) {
            return 0;
        }

        return getInsetSize(borderPercentage);
    }

    public int getInsetSize(int insetPercentage) {
        return (int)(insetPercentage/100.0*Math.min(width, height));
    }

    // NOTE: This only works for panels where the width is greater than the height
    private void drawDualColoredBackground(Graphics g) {
        int smallWidth = (width - height)/2;
        int largeWidth = width - smallWidth;

        g.translate(x, y);

        // (0, 0) -> (largeWidth, 0) -> (smallWidth, height) -> (0, height) -> (0, 0)
        g.setColor(backgroundColor);
        g.fillPolygon(new int[] { 0, largeWidth, smallWidth, 0 }, new int[] { 0, 0, height, height }, 4);

        // (largeWidth, 0) -> (smallWidth, height) -> (width, height) -> (width, 0) -> (largeWidth, 0)
        g.setColor(secondBackgroundColor);
        g.fillPolygon(new int[] { largeWidth, smallWidth, width, width }, new int[] { 0, height, height, 0 }, 4);

        g.translate(-x, -y);
    }

    public void fillBar(Graphics g, Color color, double ratio) {
        ratio = Math.min(1, ratio);

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

    public void drawBackground(Graphics g) {
        // If not full transparency, draw the background colors
        if (!onlyTransparency) {
            if (secondBackgroundColor == null) {
                g.setColor(backgroundColor);
                g.fillRect(x, y, width, height);
            } else {
                drawDualColoredBackground(g);
            }
        }

        if (greyOut) {
            DrawUtils.greyOut(g, x, y, width, height);
            greyOut = false;
        }

        int borderSize = this.getBorderSize();
        if (transparentBackground) {

            for (int i = 0; i < transparentCount; i++) {
                DrawUtils.fillTransparent(g, x + borderSize, y + borderSize, width - 2*borderSize, height - 2*borderSize);
            }
        } else {
            DrawUtils.drawBorder(g, borderColor, x, y, width, height, borderSize);
        }

        blackOutline(g);
    }

    public Button[] getButtons(int spacing, int numRows, int numCols) {
        return this.getButtons(spacing, numRows, numCols, 0, null);
    }

    public Button[] getButtons(int spacing, int numRows, int numCols, int startIndex, int[] defaultTransitions) {
        return this.getButtons(spacing, numRows, numCols, numRows, numCols, startIndex, defaultTransitions);
    }

    public Button[] getButtons(int spacing, int numSpaceRows, int numSpaceCols, int numButtonRows, int numButtonCols, int startIndex, int[] defaultTransitions) {
        int buttonWidth = (this.width - (numSpaceCols + 1)*spacing)/numSpaceCols;
        int buttonHeight = (this.height - (numSpaceRows + 1)*spacing)/numSpaceRows;

        return this.getButtons(buttonWidth, buttonHeight, numSpaceRows, numSpaceCols, numButtonRows, numButtonCols, startIndex, defaultTransitions);
    }

    public Button[] getButtons(int buttonWidth, int buttonHeight, int numRows, int numCols) {
        return this.getButtons(buttonWidth, buttonHeight, numRows, numCols, numRows, numCols, 0, null);
    }

    public Button[] getButtons(
            int buttonWidth, int buttonHeight,
            int numSpaceRows, int numSpaceCols,
            int numButtonRows, int numButtonCols,
            int startValue, int[] defaultTransitions) {
        int borderSize = this.getBorderSize();

        int horizontalSpacing = this.width - 2*borderSize - numSpaceCols*buttonWidth;
        int verticalSpacing = this.height - 2*borderSize - numSpaceRows*buttonHeight;

        int xSpacing = horizontalSpacing/(numSpaceCols + 1);
        int ySpacing = verticalSpacing/(numSpaceRows + 1);

        Button[] buttons = new Button[numButtonRows*numButtonCols];
        for (int row = 0, index = 0; row < numButtonRows; row++) {
            for (int col = 0; col < numButtonCols; col++, index++) {
                buttons[index] = new Button(
                        this.x + borderSize + xSpacing*(col + 1) + buttonWidth*col,
                        this.y + borderSize + ySpacing*(row + 1) + buttonHeight*row,
                        buttonWidth,
                        buttonHeight,
                        ButtonHoverAction.BOX,
                        Button.getBasicTransitions(index, numButtonRows, numButtonCols, startValue, defaultTransitions)
                );
            }
        }

        return buttons;
    }

    private int getTextSpace(Graphics g) {
        return this.getBorderSize() + FontMetrics.getDistanceBetweenRows(g)/2;
    }

    public int drawMessage(Graphics g, int fontSize, String text) {
        g.setColor(Color.BLACK);

        FontMetrics.setFont(g, fontSize);
        int textSpace = this.getTextSpace(g);

        int startX = x + textSpace;
        int startY = y + textSpace + FontMetrics.getTextHeight(g);

        int textWidth = width - 2*textSpace;

        return DrawUtils.drawWrappedText(g, text, startX, startY, textWidth);
    }

    public void drawLeftLabel(Graphics g, int fontSize, String label) {
        int startX = x + this.getTextSpace(g);
        int centerY = centerY();

        FontMetrics.setFont(g, fontSize);
        DrawUtils.drawCenteredHeightString(g, label, startX, centerY);
    }

    public void imageLabel(Graphics g, BufferedImage image) {
        DrawUtils.drawCenteredImage(g, image, x + width/2, y + height/2);
    }

    public int centerX() {
        return x + width/2;
    }

    public int centerY() {
        return y + height/2;
    }
}
