package gui.panel;

import util.DrawUtils;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;

public class MessagePanel extends DrawPanel {

    public MessagePanel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public MessagePanel withBorderColor(Color borderColor) {
        return (MessagePanel)super.withBorderColor(borderColor);
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
