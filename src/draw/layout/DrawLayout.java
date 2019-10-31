package draw.layout;

import draw.panel.DrawPanel;
import draw.panel.Panel;
import main.Global;

public class DrawLayout {
    protected final DrawPanel outerPanel;

    protected final int numRows;
    protected final int numCols;

    private final int spacing;
    private final int width;
    private final int height;

    private int missingRows;
    protected DrawPanelIndexSetup drawSetup;

    public DrawLayout(DrawPanel panel, int numRows, int numCols, int spacing) {
        this(panel, numRows, numCols, spacing, -1, -1);
    }

    public DrawLayout(DrawPanel panel, int numRows, int numCols, Panel size) {
        this(panel, numRows, numCols, size.getWidth(), size.getHeight());
    }

    public DrawLayout(DrawPanel panel, int numRows, int numCols, int width, int height) {
        this(panel, numRows, numCols, -1, width, height);
    }

    private DrawLayout(DrawPanel panel, int numRows, int numCols, int spacing, int width, int height) {
        this.outerPanel = panel;
        this.numRows = numRows;
        this.numCols = numCols;
        this.spacing = spacing;
        this.width = width;
        this.height = height;

        // Default values
        this.missingRows = 0;
        this.drawSetup = (drawPanel, index) -> {};
    }

    public ButtonLayout asButtonLayout() {
        if (this instanceof ButtonLayout) {
            return (ButtonLayout)this;
        }

        Global.error("Must already be a ButtonLayout.");
        return new ButtonLayout(this.outerPanel, 0, 0, 0);
    }

    public DrawLayout withMissingBottomRow() {
        this.missingRows = 1;
        return this;
    }

    public DrawLayout withDrawSetup(DrawPanelSetup drawSetup) {
        this.drawSetup = this.drawSetup.add((panel, index) -> drawSetup.setup(panel));
        return this;
    }

    public DrawLayout withDrawSetup(DrawPanelIndexSetup drawSetup) {
        this.drawSetup = this.drawSetup.add(drawSetup);
        return this;
    }

    public DrawPanel[] getPanels() {
        int borderSize = outerPanel.getBorderSize();

        int numSpaceRows = numRows + missingRows;
        int numSpaceCols = numCols;

        int panelWidth;
        int panelHeight;
        if (this.spacing != -1) {
            panelWidth = (outerPanel.width - 2*borderSize - (numSpaceCols + 1)*spacing)/numSpaceCols;
            panelHeight = (outerPanel.height - 2*borderSize - (numSpaceRows + 1)*spacing)/numSpaceRows;
        } else {
            panelWidth = width;
            panelHeight = height;
        }

        int horizontalSpacing = outerPanel.width - 2*borderSize - numSpaceCols*panelWidth;
        int verticalSpacing = outerPanel.height - 2*borderSize - numSpaceRows*panelHeight;

        int xSpacing = horizontalSpacing/(numSpaceCols + 1);
        int ySpacing = verticalSpacing/(numSpaceRows + 1);

        DrawPanel[] panels = new DrawPanel[numRows*numCols];
        for (int row = 0, index = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++, index++) {
                panels[index] = new DrawPanel(
                        outerPanel.x + borderSize + xSpacing*(col + 1) + panelWidth*col,
                        outerPanel.y + borderSize + ySpacing*(row + 1) + panelHeight*row,
                        panelWidth,
                        panelHeight
                );
                drawSetup.setup(panels[index], index);
            }
        }

        return panels;
    }

    @FunctionalInterface
    public interface DrawPanelSetup {
        void setup(DrawPanel panel);
    }

    @FunctionalInterface
    public interface DrawPanelIndexSetup {
        void setup(DrawPanel panel, int index);

        default DrawPanelIndexSetup add(DrawPanelIndexSetup next) {
            return (panel, index) -> {
                this.setup(panel, index);
                next.setup(panel, index);
            };
        }
    }
}
