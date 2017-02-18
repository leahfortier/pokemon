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

    public static int getTextWidth(Graphics g, String text) {
        return getTextWidth(g.getFont().getSize(), text);
    }

    public static int getTextWidth(int fontSize, String text) {
        FontMetrics fontMetrics = getFontMetrics(fontSize);
        return fontMetrics.getTextLength(text);
    }

    public static int getSuggestedWidth(String text, int fontSize) {
        FontMetrics fontMetrics = getFontMetrics(fontSize);
        return (text.length() + 2)*fontMetrics.horizontalSpacing;
    }

    public static FontMetrics getFontMetrics(int fontSize) {
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

    public static int getTextHeight(int fontSize) {
        FontMetrics fontMetrics = getFontMetrics(fontSize);
        return fontMetrics.letterHeight;
    }

    public static int getTextHeight(Graphics g) {
		return getTextHeight(g.getFont().getSize());
	}

    public static int getDistanceBetweenRows(Graphics g) {
        int fontSize = g.getFont().getSize();
        FontMetrics fontMetrics = getFontMetrics(fontSize);
        return (int)(fontMetrics.letterHeight*VERTICAL_WRAP_FACTOR);
    }

    public int getTextLength(String s) {
        return s.length()*this.getHorizontalSpacing();
    }

    public int getHorizontalSpacing() {
        return this.horizontalSpacing;
    }

    public int getLetterHeight() {
        return this.letterHeight;
    }

    public String toString() {
        return fontSize + " " + horizontalSpacing + " " + letterHeight;
    }
}
