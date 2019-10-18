package draw.panel;

import util.GeneralUtils;

import java.awt.Graphics;
import java.util.Arrays;
import java.util.List;

public class PanelList {
    private final DrawPanel[] panels;

    public PanelList(List<DrawPanel> listPanels, DrawPanel[] arrayPanels) {
        this(GeneralUtils.combine(listPanels, Arrays.asList(arrayPanels)).toArray(new DrawPanel[0]));
    }

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
