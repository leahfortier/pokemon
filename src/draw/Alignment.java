package draw;

import util.FontMetrics;

import java.awt.Graphics;

public enum Alignment {
    RIGHT(Alignment::drawRightAlignedString),
    CENTER(Alignment::drawCenteredString),
    CENTER_X(Alignment::drawCenteredWidthString),
    CENTER_Y(Alignment::drawCenteredHeightString),
    LEFT(Graphics::drawString);

    private final DrawAlignment drawAlignment;

    Alignment(DrawAlignment drawAlignment) {
        this.drawAlignment = drawAlignment;
    }

    public static void drawCenteredWidthString(Graphics g, String s, int centerX, int y) {
        int fontSize = g.getFont().getSize();
        FontMetrics fontMetrics = FontMetrics.getFontMetrics(fontSize);

        int leftX = centerX(centerX, s, fontMetrics);
        g.drawString(s, leftX, y);
    }

    public static void drawCenteredHeightString(Graphics g, String s, int x, int centerY) {
        int fontSize = g.getFont().getSize();
        FontMetrics fontMetrics = FontMetrics.getFontMetrics(fontSize);

        int bottomY = centerY(centerY, fontMetrics);
        g.drawString(s, x, bottomY);
    }

    public static void drawRightAlignedString(Graphics g, String s, int rightX, int y) {
        int fontSize = g.getFont().getSize();
        FontMetrics fontMetrics = FontMetrics.getFontMetrics(fontSize);

        int leftX = rightX(rightX, s, fontMetrics);

        g.drawString(s, leftX, y);
    }

    public static void drawCenteredString(Graphics g, String s, int x, int y, int width, int height) {
        int centerX = x + width/2;
        int centerY = y + height/2;

        drawCenteredString(g, s, centerX, centerY);
    }

    public static void drawCenteredString(Graphics g, String s, int centerX, int centerY) {
        int fontSize = g.getFont().getSize();
        FontMetrics fontMetrics = FontMetrics.getFontMetrics(fontSize);

        int leftX = centerX(centerX, s, fontMetrics);
        int bottomY = centerY(centerY, fontMetrics);

        g.drawString(s, leftX, bottomY);
    }

    private static int centerY(int centerY, FontMetrics fontMetrics) {
        return centerY + fontMetrics.getLetterHeight()/2;
    }

    private static int centerX(int centerX, String s, FontMetrics fontMetrics) {
        return centerX - fontMetrics.getTextLength(s)/2;
    }

    private static int rightX(int rightX, String s, FontMetrics fontMetrics) {
        return rightX - fontMetrics.getTextLength(s);
    }

    public void drawString(Graphics g, String text, int x, int y) {
        this.drawAlignment.drawString(g, text, x, y);
    }

    private interface DrawAlignment {
        void drawString(Graphics g, String text, int x, int y);
    }
}
