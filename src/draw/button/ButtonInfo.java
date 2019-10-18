package draw.button;

import java.util.function.Consumer;

public class ButtonInfo {
    private final ButtonHoverAction hoverAction;
    private ButtonTransitions transitions;
    private ButtonPressAction pressAction;
    private Consumer<ButtonPanel> panelSetup;

    public ButtonInfo(ButtonHoverAction hoverAction) {
        this.hoverAction = hoverAction;
        this.panelSetup = buttonPanel -> {};
    }

    public ButtonInfo() {
        this(ButtonHoverAction.BOX);
    }

    public ButtonInfo transition(ButtonTransitions transitions) {
        this.transitions = transitions;
        return this;
    }

    public ButtonInfo press(ButtonPressAction pressAction) {
        this.pressAction = pressAction;
        return this;
    }

    public ButtonInfo setup(Consumer<ButtonPanel> panelSetup) {
        // TODO: This probably doesn't need the null check once everything is a little more organized
        // Right now still needs it from the Button constructors
        if (panelSetup == null) {
            return this;
        }

        // Add the setup info the end
        this.panelSetup = add(this.panelSetup, panelSetup);
        return this;
    }

    private Consumer<ButtonPanel> add(Consumer<ButtonPanel> base, Consumer<ButtonPanel> addition) {
        return panel -> {
            base.accept(panel);
            addition.accept(panel);
        };
    }

    ButtonHoverAction getHoverAction() {
        return this.hoverAction;
    }

    int[] getTransitions() {
        if (transitions == null) {
            transitions = new ButtonTransitions();
        }
        return transitions.getTransitions();
    }

    ButtonPressAction getPressAction() {
        return this.pressAction == null ? () -> {} : this.pressAction;
    }

    Consumer<ButtonPanel> getPanelSetup() {
        return this.panelSetup;
    }
}
