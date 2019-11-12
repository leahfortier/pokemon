package draw.layout;

import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonPanel.ButtonPanelIndexSetup;
import draw.button.ButtonPanel.ButtonPanelSetup;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.panel.DrawPanel;
import draw.panel.Panel;
import util.Point;

public class ButtonLayout extends DrawLayout {
    private int startIndex;
    private ButtonTransitions defaultTransitions;
    private ButtonIndexAction indexAction;
    private ButtonPanelIndexSetup buttonSetup;
    private ButtonHoverAction hoverAction;

    public ButtonLayout(DrawPanel panel, int numRows, int numCols, int spacing) {
        super(panel, numRows, numCols, spacing);
        setDefaultValues();
    }

    public ButtonLayout(DrawPanel panel, int numRows, int numCols, Panel sizing) {
        this(panel, numRows, numCols, sizing.getWidth(), sizing.getHeight());
    }

    public ButtonLayout(DrawPanel panel, int numRows, int numCols, int width, int height) {
        super(panel, numRows, numCols, width, height);
        setDefaultValues();
    }

    private void setDefaultValues() {
        this.startIndex = 0;
        this.defaultTransitions = null;
        this.indexAction = index -> {};
        this.buttonSetup = (panel, index) -> {};
        this.hoverAction = ButtonHoverAction.BOX;
    }

    @Override
    public ButtonLayout withMissingBottomRow() {
        return (ButtonLayout)super.withMissingBottomRow();
    }

    @Override
    public ButtonLayout withMissingTopRow() {
        return (ButtonLayout)super.withMissingTopRow();
    }

    @Override
    public ButtonLayout withMissingRightCols(int missingCols) {
        return (ButtonLayout)super.withMissingRightCols(missingCols);
    }

    @Override
    public ButtonLayout withXOffset(int offset) {
        return (ButtonLayout)super.withXOffset(offset);
    }

    @Override
    public ButtonLayout withDrawSetup(DrawPanelSetup drawSetup) {
        return (ButtonLayout)super.withDrawSetup(drawSetup);
    }

    @Override
    public ButtonLayout withDrawSetup(DrawPanelIndexSetup drawSetup) {
        return (ButtonLayout)super.withDrawSetup(drawSetup);
    }

    public ButtonLayout withStartIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    public ButtonLayout withDefaultTransitions(ButtonTransitions defaultTransitions) {
        this.defaultTransitions = defaultTransitions;
        return this;
    }

    public ButtonLayout withPressIndex(ButtonIndexAction indexAction) {
        this.indexAction = indexAction;
        return this;
    }

    public ButtonLayout withPressIndex(ButtonGridIndexAction indexAction) {
        this.indexAction = index -> {
            Point point = Point.getPointAtIndex(index, this.numCols);
            int col = point.x;
            int row = point.y;
            indexAction.pressButton(row, col);
        };
        return this;
    }

    public ButtonLayout withButtonSetup(ButtonPanelIndexSetup buttonSetup) {
        this.buttonSetup = this.buttonSetup.add(buttonSetup);
        return this;
    }

    public ButtonLayout withButtonSetup(ButtonPanelSetup buttonSetup) {
        return this.withButtonSetup((panel, index) -> buttonSetup.setup(panel));
    }

    public ButtonLayout withArrowHover() {
        this.hoverAction = ButtonHoverAction.ARROW;
        return this;
    }

    private Button createButton(DrawPanel drawPanel, ButtonTransitions transitions, ButtonPressAction pressAction, int index) {
        return new Button(drawPanel, hoverAction, transitions, pressAction, panel -> {
            drawSetup.setup(panel, index);
            buttonSetup.setup(panel, index);
        });
    }

    // Creates a specific button that may be outside of the original layout size
    public Button getButton(int row, int col, ButtonTransitions transitions, ButtonPressAction pressAction) {
        DrawPanel drawPanel = this.getPanel(row, col);
        return this.createButton(drawPanel, transitions, pressAction, -1);
    }

    public Button[] getButtons() {
        // Get the panels with the correct sizing
        DrawPanel[] panels = this.getPanels();

        // Translate each panel into a button
        Button[] buttons = new Button[panels.length];
        for (int i = 0; i < buttons.length; i++) {
            // Silly Java, final variables are for kids
            final int index = i;

            // Setup default transitions
            ButtonTransitions transitions = ButtonTransitions.getBasicTransitions(
                    i, numRows, numCols, startIndex, defaultTransitions
            );

            // Remove the index factor from the PressAction
            ButtonPressAction pressAction = () -> indexAction.pressButton(index);

            // Create the button with all them specs
            buttons[i] = this.createButton(panels[i], transitions, pressAction, index);
        }

        return buttons;
    }

    // Returns the buttons as a grid instead of as an array
    public Button[][] getGridButtons() {
        Button[] buttons = this.getButtons();
        Button[][] gridButtons = new Button[numRows][numCols];
        for (int i = 0, index = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++, index++) {
                gridButtons[i][j] = buttons[index];
            }
        }
        return gridButtons;
    }

    @FunctionalInterface
    public interface ButtonIndexAction {
        void pressButton(int index);
    }

    @FunctionalInterface
    public interface ButtonGridIndexAction {
        void pressButton(int row, int col);
    }
}
