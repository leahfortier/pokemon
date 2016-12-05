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
    protected final int x;
    protected final int y;
    protected final int width;
    protected final int height;

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

    protected int getBorderSize() {
        return (int)(borderPercentage/100.0*Math.min(width, height));
    }

    public void drawBackground(Graphics g) {
        g.setColor(backgroundColor);

        if (transparentBackground && backgroundColor == null) {
            DrawUtils.fillTransparent(g, x, y, width, height);
        } else {
            g.fillRect(x, y, width, height);

            int borderSize = this.getBorderSize();

            if (transparentBackground) {
                DrawUtils.fillTransparent(g, x + borderSize, y + borderSize, width - 2*borderSize, height - 2*borderSize);
            } else {
                DrawUtils.drawBorder(g, borderColor, x, y, width, height, borderSize);
            }
        }

        DrawUtils.blackOutline(g, x, y, width, height, outlineDirections);
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
