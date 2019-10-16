package draw;

import util.FontMetrics;
import util.string.StringAppender;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class TextWrapper {
    private final String text;
    private final int x;
    private final int y;
    private final int width;

    private int lastY;
    private int nextY;

    public TextWrapper(String text, int x, int y, int width) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public int numRows(Graphics g) {
        return this.getRows(g, -1).length;
    }

    private String[] getRows(Graphics g, int lastWordActualLength) {
        FontMetrics fontMetrics = FontMetrics.getFontMetrics(g);

        String[] words = text.split("[ ]+");
        StringAppender appender = new StringAppender();

        List<String> rows = new ArrayList<>();

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
                rows.add(appender.toString());
                appender.clear();
            }

            appender.appendDelimiter(" ", word);
        }

        rows.add(appender.toString());
        return rows.toArray(new String[0]);
    }

    public TextWrapper draw(Graphics g) {
        return this.draw(g, -1);
    }

    public TextWrapper draw(Graphics g, int lastWordActualLength) {
        int textY = y;
        int distanceBetweenRows = FontMetrics.getDistanceBetweenRows(g);

        String[] rows = this.getRows(g, lastWordActualLength);
        for (String row : rows) {
            g.drawString(row, x, textY);
            textY += distanceBetweenRows;
        }

        this.lastY = textY - distanceBetweenRows;
        this.nextY = textY;
        return this;
    }

    public int nextY() {
        return this.nextY;
    }

    public boolean fits(int bottomY) {
        return this.lastY <= bottomY;
    }
}
