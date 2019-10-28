package draw.panel;

import draw.button.Button;
import draw.button.ButtonTransitions;
import draw.panel.DrawPanel.ButtonIndexAction;
import draw.panel.DrawPanel.PanelIndexSetup;

public class DrawLayout {
    private final DrawPanel outerPanel;

    private final int spacing;
    private final int width;
    private final int height;

    private final int numRows;
    private final int numCols;

    private int missingRows;

    private int startIndex;
    private ButtonTransitions defaultTransitions;
    private ButtonIndexAction indexAction;
    private PanelIndexSetup indexSetup;

    public DrawLayout(DrawPanel panel, int numRows, int numCols, int spacing) {
        this(panel, numRows, numCols, spacing, -1, -1);
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
    }

    public DrawLayout withMissingRows(int missingRows) {
        this.missingRows = missingRows;
        return this;
    }

    public DrawLayout withStartIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    public DrawLayout withDefaultTransitions(ButtonTransitions defaultTransitions) {
        this.defaultTransitions = defaultTransitions;
        return this;
    }
    public DrawLayout withIndexAction(ButtonIndexAction indexAction) {
        this.indexAction = indexAction;
        return this;
    }

    public DrawLayout withIndexSetup(PanelIndexSetup indexSetup) {
        this.indexSetup = indexSetup;
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

        DrawPanel[] panels = new DrawPanel[numRows*numRows];
        for (int row = 0, index = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++, index++) {
                panels[index] = new DrawPanel(
                        outerPanel.x + borderSize + xSpacing*(col + 1) + panelWidth*col,
                        outerPanel.y + borderSize + ySpacing*(row + 1) + panelHeight*row,
                        panelWidth,
                        panelHeight
                );
            }
        }

        return panels;
    }

    public Button[] getButtons() {
        // Get the panels with the correct sizing
        DrawPanel[] panels = this.getPanels();

        // Translate each panel into a button
        Button[] buttons = new Button[panels.length];
        for (int i = 0; i < buttons.length; i++) {
            // Setup default transitions
            ButtonTransitions transitions = ButtonTransitions.getBasicTransitions(
                    i, numRows, numCols, startIndex, defaultTransitions
            );

            // Create the button with all them specs
            buttons[i] = new Button(
                    panels[i],
                    transitions,
                    ButtonIndexAction.getPressAction(indexAction, i),
                    PanelIndexSetup.getPanelSetup(indexSetup, i)
            );
        }

        return buttons;
    }
}
