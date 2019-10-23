package draw.panel;

import util.GeneralUtils;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PanelList {
    // Order is extremely important as it is the order which the panels will be drawn in
    private final List<DrawPanel> panels;

    public PanelList(List<DrawPanel> listPanels, DrawPanel... arrayPanels) {
        this(GeneralUtils.combine(listPanels, Arrays.asList(arrayPanels)));
    }

    public PanelList(DrawPanel... panels) {
        this(Arrays.asList(panels));
    }

    public PanelList(List<DrawPanel> panels) {
        this.panels = new ArrayList<>(panels);
    }

    public void add(DrawPanel... panels) {
        this.panels.addAll(Arrays.asList(panels));
    }

    public void drawAll(Graphics g) {
        for (DrawPanel panel : panels) {
            panel.draw(g);
        }
    }
}
