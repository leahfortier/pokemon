package draw.panel;

import util.FontMetrics;

import java.awt.Graphics;

public class LabelPanel extends DrawPanel {
    private final int fontSize;
    private final String label;

    public LabelPanel(int x, int y, int fontSize, int spacing, String label) {
        super(
                x,
                y,
                FontMetrics.getTextWidth(fontSize, label) + 2*spacing,
                FontMetrics.getTextHeight(fontSize) + 2*spacing
        );

        this.withFullTransparency();
        this.withBlackOutline();

        this.fontSize = fontSize;
        this.label = label;
    }

    public LabelPanel draw(Graphics g) {
        this.drawBackground(g);
        this.label(g, fontSize, label);
        return this;
    }
}
