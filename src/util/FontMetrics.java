package util;

import main.Global;

import java.awt.Font;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FontMetrics {
    // For wrapped text, the amount in between each letter
    private static final float VERTICAL_WRAP_FACTOR = 2f;

    // The font the game interface uses
    private static Map<Integer, Font> fontMap;
    private static Map<Integer, FontMetrics> fontMetricsMap;

    private final int fontSize;
    private final int horizontalSpacing;
    private final int letterHeight;

    public FontMetrics(int fontSize, int horizontalSpacing, int letterHeight) {
        this.fontSize = fontSize;
        this.horizontalSpacing = horizontalSpacing;
        this.letterHeight = letterHeight;
    }

    public static int getSuggestedWidth(String text, int fontSize) {
        FontMetrics fontMetrics = getFontMetrics(fontSize);
        return (text.length() + 2)*fontMetrics.horizontalSpacing;
    }

    static FontMetrics getFontMetrics(int fontSize) {
        if (fontMetricsMap == null) {
            loadFontMetricsMap();
        }

        if (!fontMetricsMap.containsKey(fontSize)) {
            Global.error("No metrics for the font size " + fontSize);
        }

        return fontMetricsMap.get(fontSize);
    }

    public static void loadFontMetricsMap() {
        if (fontMetricsMap != null) {
            return;
        }

        fontMetricsMap = new HashMap<>();

        Scanner in = FileIO.openFile(FileName.FONT_METRICS);
        while (in.hasNext()) {
            int fontSize = in.nextInt();
            int horizontal = in.nextInt();
            int height = in.nextInt();

            FontMetrics fontMetrics = new FontMetrics(fontSize, horizontal, height);
            fontMetricsMap.put(fontSize, fontMetrics);
        }
    }

    public static Font getFont(int size) {
        if (fontMap == null) {
            fontMap = new HashMap<>();
        }

        if (!fontMap.containsKey(size)) {
            fontMap.put(size, new Font("Consolas", Font.BOLD, size));
        }

        return fontMap.get(size);
    }

    public static void setFont(Graphics g, int fontSize) {
        g.setFont(getFont(fontSize));
    }

    public static int getSuggestedHeight(int fontSize) {
        FontMetrics fontMetrics = getFontMetrics(fontSize);
        return (int)(fontMetrics.letterHeight*VERTICAL_WRAP_FACTOR*1.5);
    }

    public static int getTextHeight(Graphics g) {
		int fontSize = g.getFont().getSize();
		FontMetrics fontMetrics = getFontMetrics(fontSize);
		return fontMetrics.letterHeight;
	}

    public static int getDistanceBetweenRows(Graphics g) {
        int fontSize = g.getFont().getSize();
        FontMetrics fontMetrics = getFontMetrics(fontSize);
        return (int)(fontMetrics.letterHeight*VERTICAL_WRAP_FACTOR);
    }

    public static int drawWrappedText(Graphics g, String str, int x, int y, int width) {
        int fontSize = g.getFont().getSize();
        FontMetrics fontMetrics = getFontMetrics(fontSize);

        String[] words = str.split("[ ]+");
        StringBuilder build = new StringBuilder();

        int height = y;
        int distanceBetweenRows = getDistanceBetweenRows(g);

        for (String word : words) {
            if ((word.length() + build.length() + 1) * fontMetrics.horizontalSpacing > width) {
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

    public int getLength(String s) {
        return s.length()*this.getHorizontalSpacing();
    }

    public int getHorizontalSpacing() {
        return this.horizontalSpacing;
    }

    int getLetterHeight() {
        return this.letterHeight;
    }

    public String toString() {
        return fontSize + " " + horizontalSpacing + " " + letterHeight;
    }
}
