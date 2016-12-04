package gui.panel;

import gui.Button;
import gui.ButtonHoverAction;
import util.DrawUtils;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;

public class DrawPanel {
    protected final int x;
    protected final int y;
    protected final int width;
    protected final int height;

    private int borderPercentage;

    private Color bgColor;
    private Color borderColor;

    private boolean blackOutline;

    public DrawPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.borderPercentage = 10;

        this.bgColor = Color.WHITE;
        this.borderColor = Color.LIGHT_GRAY;

        this.blackOutline = false;
    }

    public DrawPanel withBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public DrawPanel withDarkBorder() {
        return this.withBorderColor(this.bgColor.darker());
    }

    public DrawPanel withBackgroundColor(Color bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public DrawPanel withBorderPercentage(int borderPercentage) {
        this.borderPercentage = borderPercentage;
        return this;
    }

    public DrawPanel withBlackOutline() {
        this.blackOutline = true;
        return this;
    }

    protected int getBorderSize() {
        return (int)(borderPercentage/100.0*Math.min(width, height));
    }

    public void drawBackground(Graphics g) {
        g.setColor(bgColor);
        g.fillRect(x, y, width, height);

        DrawUtils.drawBorder(g, borderColor, x, y, width, height, this.getBorderSize());

        if (blackOutline) {
            DrawUtils.blackOutline(g, x, y, width, height);
        }
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
