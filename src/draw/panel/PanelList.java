package draw.panel;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PanelList {
    // Order is important as it is the order which the panels will be drawn in
    private final List<DrawPanel> panels;

    public PanelList(DrawPanel... panels) {
        this.panels = new ArrayList<>();
        this.add(panels);
    }

    public PanelList add(DrawPanel... panels) {
        this.panels.addAll(Arrays.asList(panels));
        return this;
    }

    public void drawAll(Graphics g) {
        for (DrawPanel panel : panels) {
            panel.draw(g);
        }
    }
}
