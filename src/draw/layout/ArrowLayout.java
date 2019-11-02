package draw.layout;

import draw.panel.DrawPanel;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public class ArrowLayout {
    private final DrawPanel panel;

    private int arrowWidth;
    private int arrowHeight;

    public ArrowLayout(DrawPanel outerPanel) {
        this.panel = outerPanel;

        this.arrowWidth = 35;
        this.arrowHeight = 20;
    }

    public Entry<DrawPanel, DrawPanel> getPanels() {
        DrawPanel leftArrow = new DrawPanel(
                panel.x + panel.width/4,
                panel.centerY() - arrowHeight/2,
                arrowWidth,
                arrowHeight
        );

        DrawPanel rightArrow = new DrawPanel(
                panel.rightX() - (leftArrow.x - panel.x) - leftArrow.width,
                leftArrow.y,
                leftArrow.width,
                leftArrow.height
        );

        return new SimpleEntry<>(leftArrow, rightArrow);
    }
}
