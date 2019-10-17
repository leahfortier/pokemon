package draw.panel;

import util.FontMetrics;

import java.awt.Graphics;

public class LabelPanel extends DrawPanel {
    // This constructor will not only create the panel, but will not draw anything
    public LabelPanel(int x, int y, int fontSize, int spacing, String label) {
        this(null, x, y, fontSize, spacing, label);
    }

    // This constructor will immediately draw the label
    public LabelPanel(Graphics g, int x, int y, int fontSize, int spacing, String label) {
        super(
                x,
                y,
                FontMetrics.getTextWidth(fontSize, label) + 2*spacing,
                FontMetrics.getTextHeight(fontSize) + 2*spacing
        );

        this.withFullTransparency();
        this.withBlackOutline();
        this.withLabel(label, fontSize);

        if (g != null) {
            super.draw(g);
        }
    }
}
