package draw;

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

    static int getTextWidth(final String text, final int fontSize) {
        return text.length()* FontMetrics.getFontMetrics(fontSize).getHorizontalSpacing();
    }

    public static int drawWrappedText(Graphics g, String str, int x, int y, int width) {
        int fontSize = g.getFont().getSize();
        FontMetrics fontMetrics = FontMetrics.getFontMetrics(fontSize);

        String[] words = str.split("[ ]+");
        StringBuilder build = new StringBuilder();

        int height = y;
        int distanceBetweenRows = FontMetrics.getDistanceBetweenRows(g);

        for (String word : words) {
            if ((word.length() + build.length() + 1)*fontMetrics.getHorizontalSpacing() > width) {
                g.drawString(build.toString(), x, height);

                height += distanceBetweenRows;
                build = new StringBuilder();
            }

            // TODO: StringUtil method
            build.append(build.length() == 0 ? "" : " ")
                    .append(word);
        }

        g.drawString(build.toString(), x, height);

        return height + distanceBetweenRows;
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
}
