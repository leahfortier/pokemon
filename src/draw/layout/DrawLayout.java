package draw.layout;

import draw.panel.DrawPanel;
import draw.panel.Panel;
import main.Global;
import util.Point;

import java.awt.Dimension;

public class DrawLayout {
    private final DrawPanel outerPanel;

    protected final int numRows;
    protected final int numCols;

    private final int spacing;
    private final int width;
    private final int height;

    private boolean missingTop;
    private int missingRows;
    private int missingCols;

    private int xOffset;

    protected DrawPanelIndexSetup drawSetup;

    public DrawLayout(DrawPanel panel, int numRows, int numCols, int spacing) {
        this(panel, numRows, numCols, spacing, -1, -1);
    }

    public DrawLayout(DrawPanel panel, int numRows, int numCols, Panel reference) {
        this(panel, numRows, numCols, reference.getWidth(), reference.getHeight());
    }

    public DrawLayout(DrawPanel panel, int numRows, int numCols, Dimension size) {
        this(panel, numRows, numCols, size.width, size.height);
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
        this.missingCols = 0;
        this.xOffset = 0;
        this.drawSetup = (drawPanel, index) -> drawPanel.withNoBackground();
    }

    private DrawLayout withMissingRows(boolean isTop) {
        if (this.missingRows != 0) {
            Global.error("Missing rows can only be set once.");
        }

        this.missingRows = 1;
        this.missingTop = isTop;
        return this;
    }

    public DrawLayout withMissingBottomRow() {
        return this.withMissingRows(false);
    }

    public DrawLayout withMissingTopRow() {
        return this.withMissingRows(true);
    }

    public DrawLayout withMissingRightCols(int missingCols) {
        this.missingCols = missingCols;
        return this;
    }

    public DrawLayout withXOffset(int offset) {
        this.xOffset = offset;
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

    public DrawPanel getPanel(int row, int col) {
        DrawPanel[] allPanels = this.getAllPanels();
        int numCols = this.numCols + missingCols;
        return allPanels[Point.getIndex(col, row, numCols)];
    }

    // Includes the missing panels that you didn't want
    private DrawPanel[] getAllPanels() {
        int borderSize = outerPanel.getBorderSize();

        int numRows = this.numRows + missingRows;
        int numCols = this.numCols + missingCols;

        int panelWidth;
        int panelHeight;
        if (this.spacing != -1) {
            panelWidth = (outerPanel.width - 2*borderSize - (numCols + 1)*spacing)/numCols;
            panelHeight = (outerPanel.height - 2*borderSize - (numRows + 1)*spacing)/numRows;
        } else {
            panelWidth = width;
            panelHeight = height;
        }

        int horizontalSpacing = outerPanel.width - 2*borderSize - numCols*panelWidth;
        int verticalSpacing = outerPanel.height - 2*borderSize - numRows*panelHeight;

        int xSpacing = horizontalSpacing/(numCols + 1);
        int ySpacing = verticalSpacing/(numRows + 1);

        int xRemainder = horizontalSpacing%(numCols + 1);
        int yRemainder = verticalSpacing%(numRows + 1);

        DrawPanel[] panels = new DrawPanel[numRows*numCols];
        for (int row = 0, index = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++, index++) {
                panels[index] = new DrawPanel(
                        outerPanel.x + borderSize + xSpacing*(col + 1) + panelWidth*col + Math.min(col, xRemainder) + xOffset,
                        outerPanel.y + borderSize + ySpacing*(row + 1) + panelHeight*row + Math.min(row, yRemainder),
                        panelWidth - xOffset,
                        panelHeight
                );
                drawSetup.setup(panels[index], index);
            }
        }

        return panels;
    }

    public DrawPanel[] getPanels() {
        DrawPanel[] allPanels = this.getAllPanels();

        int numSpaceRows = numRows + missingRows;
        int numSpaceCols = numCols + missingCols;

        // No difference in the number of panels
        if (numSpaceRows == numRows && numSpaceCols == numCols) {
            return allPanels;
        }

        int rowStart = missingTop ? missingRows : 0;
        DrawPanel[] panels = new DrawPanel[numRows*numCols];
        for (int row = rowStart, index = 0; row < numRows + rowStart; row++) {
            for (int col = 0; col < numCols; col++, index++) {
                panels[index] = allPanels[Point.getIndex(col, row, numSpaceCols)];
            }
        }

        return panels;
    }

    // Returns a panel for the last row (includes the missing row if set)
    // If multiple columns, panel will stretch from the first column to the last
    public DrawPanel getFullBottomPanel() {
        DrawPanel[] panels = this.getAllPanels();

        // Note: it's totally cool/normal for these to be the same panel
        DrawPanel leftLastRow = panels[panels.length - numCols];
        DrawPanel rightLastRow = panels[panels.length - 1];

        return new DrawPanel(
                leftLastRow.x,
                leftLastRow.y,
                rightLastRow.rightX() - leftLastRow.x,
                leftLastRow.height
        );
    }

    // Works best when missing the bottom row
    public ArrowLayout getArrowLayout() {
        return new ArrowLayout(this.getFullBottomPanel());
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
