package draw.button;

import main.Game;

@FunctionalInterface
public interface ButtonPressAction {
    void buttonPressed();

    // Common action for return buttons and such
    static ButtonPressAction getExitAction() {
        return () -> Game.instance().popView();
    }
}
