package draw.layout;

import draw.panel.DrawPanel;

public class ArrowLayout {
    private final DrawPanel leftArrow;
    private final DrawPanel rightArrow;

    public ArrowLayout(DrawPanel outerPanel) {
        int arrowWidth = 35;
        int arrowHeight = 20;

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
