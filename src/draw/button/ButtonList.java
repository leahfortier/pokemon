package draw.button;

import map.Direction;

import java.awt.Graphics;

public class ButtonList {
    private final Button[] buttons;

    public ButtonList(Button[] buttons) {
        this.buttons = buttons;
    }

    public int size() {
        return this.buttons.length;
    }

    public Button get(int index) {
        return this.buttons[index];
    }

    public void draw(Graphics g) {
        for (Button button : buttons) {
            button.draw(g);
        }
    }

    public void setFalseHover() {
        for (Button button : buttons) {
            button.setForceHover(false);
        }
    }

    public void setInactive() {
        for (Button button : buttons) {
            button.setActive(false);
        }
    }

    public int update(int selected) {
        Direction inputDirection = Direction.consumeInputDirection();
        if (inputDirection != null) {
            selected = this.transition(selected, inputDirection);
        }

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].update(i == selected);

            if (buttons[i].isHover()) {
                selected = i;
            }
        }

        // Press overrides hover
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].isPress()) {
                selected = i;
            }
        }

        if (!buttons[selected].isForceHover()) {
            setForceHover(selected);
        }

        return selected;
    }

    private void setForceHover(int selected) {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setForceHover(selected == i && buttons[i].isActive());
        }
    }

    private int transition(int index, Direction direction) {
        int next = index;
        do {
            next = buttons[next].getTransitions().next(direction);
        } while (next != ButtonTransitions.NO_TRANSITION && !buttons[next].isActive());

        if (next == ButtonTransitions.NO_TRANSITION) {
            return index;
        }

        setForceHover(next);

        return next;
    }
}
