package draw;

import draw.panel.Panel;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;

public final class TextUtils {
    // Draws a string with a shadow behind it the specified location
    public static void drawShadowText(Graphics g, String text, int x, int y, Alignment alignment) {
        g.setColor(new Color(128, 128, 128, 128));
        alignment.drawString(g, text, x + 2, y + 2);

        g.setColor(Color.BLACK);
        alignment.drawString(g, text, x, y);
    }

    public static int getTextWidth(final String text, final int fontSize) {
        return text.length()*FontMetrics.getFontMetrics(fontSize).getHorizontalSpacing();
    }

    // Draws the text wrapping to the next line if the current line exceeds the width
    // and returns the next appropriate y to draw to
    public static int drawWrappedText(Graphics g, String text, int x, int y, int width) {
        return new TextWrapper(text, x, y, width).draw(g).nextY();
    }

    // Draws the text "<currentPage + 1>/<totalPages> centered between the two buttons
    // Does not draw the button/arrows at all
    // currentPage is expected to be zero-indexed and will be incremented for display purposes
    public static void drawPageNumbers(Graphics g, int fontSize, Panel leftArrow, Panel rightArrow, int currentPage, int totalPages) {
        FontMetrics.setFont(g, fontSize);
        g.setColor(Color.BLACK);
        drawCenteredString(
                g,
                (currentPage + 1) + "/" + totalPages,
                (leftArrow.centerX() + rightArrow.centerX())/2,
                rightArrow.centerY()
        );
    }

    public static void drawCenteredWidthString(Graphics g, String s, int centerX, int y) {
        FontMetrics fontMetrics = FontMetrics.getFontMetrics(g);

        int leftX = centerX(centerX, s, fontMetrics);
        g.drawString(s, leftX, y);
    }

    public static void drawCenteredHeightString(Graphics g, String s, int x, int centerY) {
        drawCenteredHeightString(g, s, x, centerY, Alignment.LEFT);
    }

    public static void drawCenteredHeightString(Graphics g, String s, int x, int centerY, Alignment alignment) {
        FontMetrics fontMetrics = FontMetrics.getFontMetrics(g);

        int bottomY = centerY(centerY, fontMetrics);
        alignment.drawString(g, s, x, bottomY);
    }

    public static void drawRightAlignedString(Graphics g, String s, int rightX, int y) {
        FontMetrics fontMetrics = FontMetrics.getFontMetrics(g);

        int leftX = rightX(rightX, s, fontMetrics);
        g.drawString(s, leftX, y);
    }

    public static void drawCenteredString(Graphics g, String s, int x, int y, int width, int height) {
        int centerX = x + width/2;
        int centerY = y + height/2;

        drawCenteredString(g, s, centerX, centerY);
    }

    public static void drawCenteredString(Graphics g, String s, int centerX, int centerY) {
        FontMetrics fontMetrics = FontMetrics.getFontMetrics(g);

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
}
