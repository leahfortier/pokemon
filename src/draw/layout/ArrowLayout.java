package draw.layout;

import draw.panel.DrawPanel;

public class ArrowLayout {
    public static final int arrowWidth = 35;
    public static final int arrowHeight = 20;

    private final DrawPanel leftArrow;
    private final DrawPanel rightArrow;

    public ArrowLayout(DrawPanel outerPanel) {
        leftArrow = new DrawPanel(
                outerPanel.x + outerPanel.width/4,
                outerPanel.centerY() - arrowHeight/2,
                arrowWidth,
                arrowHeight
        );

        rightArrow = new DrawPanel(
                outerPanel.rightX() - (leftArrow.x - outerPanel.x) - leftArrow.width,
                leftArrow.y,
                leftArrow.width,
                leftArrow.height
        );
    }

    public DrawPanel getLeftPanel() {
        return this.leftArrow;
    }

    public DrawPanel getRightPanel() {
        return this.rightArrow;
    }
}
