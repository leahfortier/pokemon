package draw.panel;

import util.FontMetrics;

public class LabelPanel extends DrawPanel {
    public LabelPanel(int x, int y, int fontSize, int spacing, String label) {
        super(
                x, y,
                FontMetrics.getTextWidth(fontSize, label) + 2*spacing,
                FontMetrics.getTextHeight(fontSize) + 2*spacing
        );

        this.withFullTransparency();
        this.withBlackOutline();
        this.withLabel(label, fontSize);
    }
}
