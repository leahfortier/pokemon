package draw.button;

import map.Direction;

import java.awt.Graphics;
import java.util.Arrays;
import java.util.Iterator;

public class ButtonList implements Iterable<Button> {
    private final Button[] buttons;

    private int selected;

    public ButtonList(Button[] buttons) {
        this.buttons = buttons;
        this.selected = 0;
    }

    public int size() {
        return this.buttons.length;
    }

    public Button get(int index) {
        return this.buttons[index];
    }

    public int getSelected() {
        return this.selected;
    }

    public void setSelected(int index) {
        this.selected = index;
    }

    public boolean consumeSelectedPress() {
        return this.buttons[selected].checkConsumePress();
    }

    public void draw(Graphics g) {
        drawPanels(g);
        drawHover(g);
    }

    public void drawHover(Graphics g) {
        for (Button button : buttons) {
            button.drawHover(g);
        }
    }

    public void drawPanels(Graphics g) {
        for (Button button : buttons) {
            button.drawPanel(g);
            button.drawHover(g);
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

    public void update() {
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
    }

    private void setForceHover(int selected) {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setForceHover(selected == i && buttons[i].isActive());
        }
    }

    private int transition(int index, Direction direction) {
        int next = index;
        do {
            next = buttons[next].nextTransition(direction);
        } while (next != ButtonTransitions.NO_TRANSITION && !buttons[next].isActive());

        if (next == ButtonTransitions.NO_TRANSITION) {
            return index;
        }

        setForceHover(next);

        return next;
    }

    @Override
    public Iterator<Button> iterator() {
        return Arrays.asList(buttons).iterator();
    }
}
