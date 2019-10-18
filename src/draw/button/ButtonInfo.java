package draw.button;

public class ButtonInfo {
    private final ButtonHoverAction hoverAction;
    private ButtonTransitions transitions;
    private ButtonPressAction pressAction;
    private ButtonPanelSetup panelSetup;

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

    public ButtonInfo setup(ButtonPanelSetup panelSetup) {
        // TODO: This probably doesn't need the null check once everything is a little more organized
        // Right now still needs it from the Button constructors
        if (panelSetup == null) {
            return this;
        }

        // Add the setup info the end
        this.panelSetup = ButtonPanelSetup.add(this.panelSetup, panelSetup);
        return this;
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

    ButtonPanelSetup getPanelSetup() {
        return this.panelSetup;
    }

    @FunctionalInterface
    public interface ButtonPanelSetup {
        void setup(ButtonPanel panel);

        static ButtonPanelSetup add(ButtonPanelSetup base, ButtonPanelSetup addition) {
            return panel -> {
                base.setup(panel);
                addition.setup(panel);
            };
        }
    }
}
