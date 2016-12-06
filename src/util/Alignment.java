package util;

import java.awt.Graphics;

// TODO: Move these methods inside this class
public enum Alignment {
    RIGHT(DrawUtils::drawRightAlignedString),
    CENTER(DrawUtils::drawCenteredString),
    CENTER_X(DrawUtils::drawCenteredWidthString),
    CENTER_Y(DrawUtils::drawCenteredHeightString),
    LEFT(Graphics::drawString);

    private final DrawAlignment drawAlignment;

    Alignment(DrawAlignment drawAlignment) {
        this.drawAlignment = drawAlignment;
    }

    public void drawString(Graphics g, String text, int x, int y) {
        this.drawAlignment.drawString(g, text, x, y);
    }

    private interface DrawAlignment {
        void drawString(Graphics g, String text, int x, int y);
    }
}
