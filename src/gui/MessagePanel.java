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

    private final int borderSize;

    private Color bgColor;
    private Color borderColor;

    public MessagePanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.borderSize = (int)(.1*Math.min(width, height));

        this.bgColor = Color.WHITE;
        this.borderColor = Color.GRAY;
    }

    public MessagePanel(int x, int y, int width, int height, Color borderColor) {
        this(x, y, width, height);
        this.borderColor = borderColor;
    }

    public MessagePanel(int x, int y, int width, int height, Color bgColor, Color borderColor) {
        this(x, y, width, height, borderColor);
        this.bgColor = bgColor;
    }

    public void drawBackground(Graphics g) {
        g.setColor(bgColor);
        g.fillRect(x, y, width, height);

        g.setColor(borderColor);
        g.fillRect(x, y, width, borderSize);
        g.fillRect(x, y, borderSize, height);
        g.fillRect(x, y + height - borderSize, width, borderSize);
        g.fillRect(x + width - borderSize, y, borderSize, height);
    }

    public void drawText(Graphics g, int fontSize, String text) {
        g.setColor(Color.BLACK);

        FontMetrics.setFont(g, fontSize);
        int textSpace = borderSize + FontMetrics.getDistanceBetweenRows(g)/2;

        int startX = x + textSpace;
        int startY = y + textSpace + FontMetrics.getTextHeight(g);

        int textWidth = width - 2*textSpace;

        DrawUtils.drawWrappedText(g, text, startX, startY, textWidth);
    }
}
