package gui;

import util.DrawUtils;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;

public class MessagePanel {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private int borderPercentage;

    private Color bgColor;
    private Color borderColor;

    public MessagePanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.borderPercentage = 10;

        this.bgColor = Color.WHITE;
        this.borderColor = Color.LIGHT_GRAY;
    }

    public MessagePanel withBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public MessagePanel withBackgroundColor(Color bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public MessagePanel withBorderPercentage(int borderPercentage) {
        this.borderPercentage = borderPercentage;
        return this;
    }

    private int getBorderSize() {
        return (int)(borderPercentage/100.0*Math.min(width, height));
    }

    public void drawBackground(Graphics g) {
        g.setColor(bgColor);
        g.fillRect(x, y, width, height);

        int borderSize = this.getBorderSize();

        g.setColor(borderColor);
        g.fillRect(x, y, width, borderSize);
        g.fillRect(x, y, borderSize, height);
        g.fillRect(x, y + height - borderSize, width, borderSize);
        g.fillRect(x + width - borderSize, y, borderSize, height);
    }

    public void drawText(Graphics g, int fontSize, String text) {
        g.setColor(Color.BLACK);

        FontMetrics.setFont(g, fontSize);
        int textSpace = this.getBorderSize() + FontMetrics.getDistanceBetweenRows(g)/2;

        int startX = x + textSpace;
        int startY = y + textSpace + FontMetrics.getTextHeight(g);

        int textWidth = width - 2*textSpace;

        DrawUtils.drawWrappedText(g, text, startX, startY, textWidth);
    }
}
