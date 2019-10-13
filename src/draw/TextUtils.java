package draw;

import util.FontMetrics;
import util.string.StringAppender;

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

    public static int drawWrappedText(Graphics g, String str, int x, int y, int width) {
        return drawWrappedText(g, str, -1, x, y, width);
    }

    public static int drawWrappedText(Graphics g, String str, int lastWordActualLength, int x, int y, int width) {
        FontMetrics fontMetrics = FontMetrics.getFontMetrics(g);

        String[] words = str.split("[ ]+");
        StringAppender appender = new StringAppender();

        int height = y;
        int distanceBetweenRows = FontMetrics.getDistanceBetweenRows(g);

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            // If we're at the last word and we've specified a last word length, use that, otherwise use the
            // actual length of the word
            int wordLength = i == words.length - 1 && lastWordActualLength != -1 ? lastWordActualLength : word.length();

            // Add 1 for the space
            int potentialLineLength = appender.length() + wordLength + 1;
            int potentialLineWidth = potentialLineLength*fontMetrics.getHorizontalSpacing();

            // If adding this word would be more than the width, then write this line and go to the next one
            if (potentialLineWidth > width) {
                g.drawString(appender.toString(), x, height);

                height += distanceBetweenRows;
                appender.clear();
            }

            appender.appendDelimiter(" ", word);
        }

        g.drawString(appender.toString(), x, height);

        return height + distanceBetweenRows;
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
