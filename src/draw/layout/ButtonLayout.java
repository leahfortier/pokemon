package draw.layout;

import draw.button.Button;
import draw.button.ButtonPanel.ButtonPanelSetup;
import draw.button.ButtonTransitions;
import draw.panel.DrawPanel;
import util.Point;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public class ButtonLayout extends DrawLayout {
    private int startIndex;
    private ButtonTransitions defaultTransitions;
    private ButtonIndexAction indexAction;
    private ButtonPanelSetup buttonSetup;

    private int arrowWidth;
    private int arrowHeight;

    public ButtonLayout(DrawPanel panel, int numRows, int numCols, int spacing) {
        super(panel, numRows, numCols, spacing);
        setDefaultValues();
    }

    public ButtonLayout(DrawPanel panel, int numRows, int numCols, int width, int height) {
        super(panel, numRows, numCols, width, height);
        setDefaultValues();
    }

    private void setDefaultValues() {
        this.startIndex = 0;
        this.defaultTransitions = null;
        this.indexAction = index -> {};
        this.buttonSetup = buttonPanel -> {};

        this.arrowWidth = 35;
        this.arrowHeight = 20;
    }

    @Override
    public ButtonLayout withMissingBottomRow() {
        return super.withMissingBottomRow().asButtonLayout();
    }

    @Override
    public ButtonLayout withDrawSetup(DrawPanelSetup drawSetup) {
        return super.withDrawSetup(drawSetup).asButtonLayout();
    }

    @Override
    public ButtonLayout withDrawSetup(DrawPanelIndexSetup drawSetup) {
        return super.withDrawSetup(drawSetup).asButtonLayout();
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
            int row = point.x;
            int col = point.y;
            indexAction.pressButton(row, col);
        };
        return this;
    }

    public ButtonLayout withButtonSetup(ButtonPanelSetup buttonSetup) {
        this.buttonSetup = this.buttonSetup.add(buttonSetup);
        return this;
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

            // Create the button with all them specs
            buttons[i] = new Button(
                    panels[i],
                    transitions,
                    () -> indexAction.pressButton(index),
                    panel -> {
                        drawSetup.setup(panel, index);
                        buttonSetup.setup(panel);
                    }
            );
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

    public Entry<DrawPanel, DrawPanel> getArrowPanels() {
        DrawPanel[] panels = this.getPanels();

        DrawPanel leftArrow = new DrawPanel(
                outerPanel.x + outerPanel.width/4,
                panels[panels.length - 1].centerY() + (panels[numCols].y - panels[0].y) - arrowHeight/2,
                arrowWidth,
                arrowHeight
        );

        DrawPanel rightArrow = new DrawPanel(
                outerPanel.rightX() - (leftArrow.x - outerPanel.x) - leftArrow.width,
                leftArrow.y,
                leftArrow.width,
                leftArrow.height
        );

        return new SimpleEntry<>(leftArrow, rightArrow);
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
