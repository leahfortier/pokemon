package draw;

import java.awt.Graphics;

public enum Alignment {
    RIGHT(TextUtils::drawRightAlignedString),
    CENTER(TextUtils::drawCenteredString),
    CENTER_X(TextUtils::drawCenteredWidthString),
    CENTER_Y(TextUtils::drawCenteredHeightString),
    LEFT(Graphics::drawString);
    
    private final DrawAlignment drawAlignment;
    
    Alignment(DrawAlignment drawAlignment) {
        this.drawAlignment = drawAlignment;
    }
    
    public void drawString(Graphics g, String text, int x, int y) {
        this.drawAlignment.drawString(g, text, x, y);
    }
    
    @FunctionalInterface
    private interface DrawAlignment {
        void drawString(Graphics g, String text, int x, int y);
    }
}
