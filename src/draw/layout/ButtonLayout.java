package draw.layout;

import draw.button.Button;
import draw.button.ButtonPanel.ButtonPanelSetup;
import draw.button.ButtonTransitions;
import draw.panel.DrawPanel;

public class ButtonLayout extends DrawLayout {
    private int startIndex;
    private ButtonTransitions defaultTransitions;
    private ButtonIndexAction indexAction;
    private ButtonPanelSetup buttonSetup;

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

    @FunctionalInterface
    public interface ButtonIndexAction {
        void pressButton(int index);
    }
}
