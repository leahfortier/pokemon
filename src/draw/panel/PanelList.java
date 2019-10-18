package draw.panel;

import java.awt.Graphics;

public class PanelList {
    private final DrawPanel[] panels;

    // Order is extremely important as it is the order which the panels will be drawn in
    public PanelList(DrawPanel... panels) {
        this.panels = panels;
    }

    public void drawAll(Graphics g) {
        for (DrawPanel panel : panels) {
            panel.draw(g);
        }
    }
}
