package util;

import main.Global;
import util.file.FileIO;
import util.file.FileName;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static util.file.FileName.*;

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

    public int getTextLength(String s) {
        return this.getTextLength(s.length());
    }

    public int getTextLength(int length) {
        return length*this.getHorizontalSpacing();
    }

    public int getHorizontalSpacing() {
        return this.horizontalSpacing;
    }

    public int getLetterHeight() {
        return this.letterHeight;
    }

    @Override
    public String toString() {
        return fontSize + " " + horizontalSpacing + " " + letterHeight;
    }

    // Returns the width of a single character
    public static int getTextWidth(Graphics g) {
        return getTextWidth(g.getFont().getSize());
    }

    public static int getTextWidth(int fontSize) {
        return getTextWidth(fontSize, 1);
    }

    public static int getTextWidth(Graphics g, String text) {
        return getTextWidth(g, text.length());
    }

    public static int getTextWidth(Graphics g, int textLength) {
        return getTextWidth(g.getFont().getSize(), textLength);
    }

    public static int getTextWidth(int fontSize, String text) {
        return getTextWidth(fontSize, text.length());
    }

    public static int getTextWidth(int fontSize, int textLength) {
        FontMetrics fontMetrics = getFontMetrics(fontSize);
        return fontMetrics.getTextLength(textLength);
    }

    public static int getSuggestedWidth(String text, Graphics g) {
        return getSuggestedWidth(text, g.getFont().getSize());
    }

    public static int getSuggestedWidth(String text, int fontSize) {
        FontMetrics fontMetrics = getFontMetrics(fontSize);
        return (text.length() + 2)*fontMetrics.horizontalSpacing;
    }

    public static FontMetrics getFontMetrics(Graphics g) {
        return getFontMetrics(g.getFont().getSize());
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

            try {
                Font fb = Font.createFont(Font.TRUETYPE_FONT, new File(CONSOLAS_BOLD));

                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(fb);
            } catch (FontFormatException|IOException e) {
                Global.error("Could not create fonts:" + e);
            }
        }

        if (!fontMap.containsKey(size)) {
            fontMap.put(size, new Font("Consolas", Font.BOLD, size));
        }

        return fontMap.get(size);
    }

    public static void setBlackFont(Graphics g, int fontSize) {
        setFont(g, fontSize, Color.BLACK);
    }

    public static void setFont(Graphics g, int fontSize, Color color) {
        setFont(g, fontSize);
        g.setColor(color);
    }

    public static void setFont(Graphics g, int fontSize) {
        g.setFont(getFont(fontSize));
    }

    public static int getSuggestedHeight(Graphics g) {
        return getSuggestedHeight(g.getFont().getSize());
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

    // Sets the new font size and returns the average between rows distance with this font size and the previous one
    public static int getDistanceBetweenRows(Graphics g, int newFontSize) {
        int previousDistanceBetweenRows = getDistanceBetweenRows(g);
        setFont(g, newFontSize);
        return (previousDistanceBetweenRows + getDistanceBetweenRows(g))/2;
    }

    public static int getDistanceBetweenRows(Graphics g) {
        FontMetrics fontMetrics = getFontMetrics(g);
        return (int)(fontMetrics.letterHeight*VERTICAL_WRAP_FACTOR);
    }
}
