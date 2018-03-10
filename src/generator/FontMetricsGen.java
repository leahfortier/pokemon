package generator;

import main.Global;
import util.FontMetrics;
import util.file.FileIO;
import util.file.FileName;
import util.string.StringAppender;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

class FontMetricsGen extends JPanel {
    private static final String s = "WWWWWWWWWWWWWWWWWWWWWW";
    private static final int SMALLEST_FONT_SIZE = 1;
    private static final int LARGEST_FONT_SIZE = 150;

    private final int startX;
    private final int startY;

    private final BufferedImage canvas;
    private final Graphics2D g;
    private final int[][] colors;

    private FontMetricsGen(int width, int height) {
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        colors = new int[canvas.getWidth()][canvas.getHeight()];

        startX = 1;
        startY = canvas.getHeight()/2;

        g = canvas.createGraphics();

        StringAppender appender = new StringAppender();

        for (int fontSize = SMALLEST_FONT_SIZE; fontSize <= LARGEST_FONT_SIZE; fontSize++) {
            reset(fontSize);

            FontMetrics fontMetrics = getMetrics(fontSize);
            checkMatch(fontSize, fontMetrics.getHorizontalSpacing());
            appender.appendLine(fontMetrics.toString());
        }

        FileIO.writeToFile(FileName.FONT_METRICS, appender.toString());
    }

    private void reset(int fontSize) {
        fillCanvas(Color.WHITE);

        drawString(fontSize, startX, startY);
        setColors();
    }

    private FontMetrics getMetrics(int fontSize) {
        int leftMost = canvas.getWidth() + 1;
        int rightMost = -1;
        int highest = canvas.getHeight() + 1;
        int lowest = -1;

        for (int x = 0; x < canvas.getWidth(); x++) {
            for (int y = 0; y < canvas.getHeight(); y++) {
                if (canvas.getRGB(x, y) == Color.BLACK.getRGB()) {
                    leftMost = Math.min(leftMost, x);
                    rightMost = Math.max(rightMost, x);
                    highest = Math.min(highest, y);
                    lowest = Math.max(lowest, y);
                }
            }
        }

        double horizontalSpacing = (double)rightMost/s.length();
        int horizontalGuess = (int)(horizontalSpacing + .5);

        int letterHeight = lowest - highest;

        return new FontMetrics(fontSize, horizontalGuess, letterHeight);
    }

    private void setColors() {
        for (int x = 0; x < canvas.getWidth(); x++) {
            for (int y = 0; y < canvas.getHeight(); y++) {
                colors[x][y] = canvas.getRGB(x, y);
            }
        }
    }

    private void checkMatch(int fontSize, int spacing) {
        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            g.drawString(charArray[i] + "", startX + spacing*i, startY);
        }

        boolean match = true;
        for (int x = 0; x < canvas.getWidth(); x++) {
            for (int y = 0; y < canvas.getHeight(); y++) {
                if (colors[x][y] != canvas.getRGB(x, y)) {
                    match = false;
                    break;
                }
            }
        }

        if (!match) {
            Global.error("Could not find font metrics for font size " + fontSize + ".");
        }
    }

    private void drawString(int fontSize, int x, int y) {
        FontMetrics.setFont(g, fontSize);
        g.setColor(Color.BLACK);

        g.drawString(s, x, y);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(canvas.getWidth(), canvas.getHeight());
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;
        g2.drawImage(canvas, null, null);
    }

    private void fillCanvas(Color c) {
        int color = c.getRGB();

        for (int x = 0; x < canvas.getWidth(); x++) {
            for (int y = 0; y < canvas.getHeight(); y++) {
                canvas.setRGB(x, y, color);
            }
        }

        super.repaint();
    }

    static void writeFontMetrics() {
        int width = 1850;
        int height = 480;

        JFrame frame = new JFrame();
        FontMetricsGen panel = new FontMetricsGen(width, height);

        frame.add(panel);
        frame.pack();
        frame.dispose();
    }
}
