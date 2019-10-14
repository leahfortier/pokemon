package draw;

import util.FontMetrics;
import util.string.StringAppender;

import java.awt.Graphics;

public class TextWrapper {
    private final int lastY;
    private final int distanceBetweenRows;

    public TextWrapper(Graphics g, String str, int x, int y, int width) {
        this(g, str, -1, x, y, width);
    }

    public TextWrapper(Graphics g, String str, int lastWordActualLength, int x, int y, int width) {
        FontMetrics fontMetrics = FontMetrics.getFontMetrics(g);

        String[] words = str.split("[ ]+");
        StringAppender appender = new StringAppender();

        int textY = y;
        this.distanceBetweenRows = FontMetrics.getDistanceBetweenRows(g);

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
                g.drawString(appender.toString(), x, textY);

                textY += distanceBetweenRows;
                appender.clear();
            }

            appender.appendDelimiter(" ", word);
        }

        g.drawString(appender.toString(), x, textY);

        this.lastY = textY;
    }

    public int nextY() {
        return this.lastY + this.distanceBetweenRows;
    }

    public boolean fits(int bottomY) {
        return this.lastY <= bottomY;
    }
}
