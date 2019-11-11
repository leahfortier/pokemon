package draw.button;

import draw.button.ButtonPanel.ButtonPanelSetup;
import draw.panel.DrawPanel;
import draw.panel.Panel;
import input.ControlKey;
import input.InputControl;
import map.Direction;
import util.Point;

import java.awt.Graphics;

public class Button implements Panel {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    private final ButtonHoverAction hoverAction;
    private final ButtonPressAction pressAction;
    private final int[] transitions;

    private final ButtonPanel drawPanel;

    private boolean hover;
    private boolean press;
    private boolean forceHover;
    private boolean active;

    public Button(DrawPanel panel, ButtonTransitions transitions) {
        this(panel, transitions, null);
    }

    public Button(DrawPanel panel, ButtonTransitions transitions, ButtonPressAction pressAction) {
        this(panel, transitions, pressAction, null);
    }

    public Button(DrawPanel panel, ButtonTransitions transitions, ButtonPressAction pressAction, ButtonPanelSetup panelSetup) {
        this(panel, ButtonHoverAction.BOX, transitions, pressAction, panelSetup);
    }

    public Button(DrawPanel panel, ButtonHoverAction hoverAction, ButtonTransitions transitions, ButtonPressAction pressAction, ButtonPanelSetup panelSetup) {
        this(panel.x, panel.y, panel.width, panel.height, hoverAction, transitions, pressAction, panelSetup);
    }

    public Button(int x, int y, int width, int height) {
        this(x, y, width, height, ButtonHoverAction.BOX, null, null, null);
    }

    public Button(int x, int y, int width, int height, ButtonHoverAction hoverAction, ButtonTransitions transitions) {
        this(x, y, width, height, hoverAction, transitions, null);
    }

    public Button(int x, int y, int width, int height, ButtonHoverAction hoverAction, ButtonTransitions transitions, ButtonPressAction pressAction) {
        this(x, y, width, height, hoverAction, transitions, pressAction, null);
    }

    public Button(int x, int y, int width, int height, ButtonTransitions transitions, ButtonPressAction pressAction) {
        this(x, y, width, height, transitions, pressAction, null);
    }

    // Can we just admit that BOX is the default hover and not null and stop specifying it everywhere
    public Button(int x, int y, int width, int height, ButtonTransitions transitions,
                  ButtonPressAction pressAction, ButtonPanelSetup panelSetup) {
        this(x, y, width, height, ButtonHoverAction.BOX, transitions, pressAction, panelSetup);
    }

    public Button(int x, int y, int width, int height, ButtonHoverAction hoverAction, ButtonTransitions transitions,
                  ButtonPressAction pressAction, ButtonPanelSetup panelSetup) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.hoverAction = hoverAction;
        this.pressAction = pressAction == null ? () -> {} : pressAction;

        if (transitions == null) {
            transitions = new ButtonTransitions();
        }
        this.transitions = transitions.getTransitions();

        this.drawPanel = new ButtonPanel(this, panelSetup);

        this.hover = false;
        this.press = false;
        this.forceHover = false;
        this.active = true;
    }

    public Button asArrow(Direction arrowDirection) {
        this.panel().asArrow(arrowDirection);
        return this;
    }

    public Button setup(ButtonPanelSetup panelSetup) {
        panelSetup.setup(this.panel());
        return this;
    }

    public ButtonPanel panel() {
        return this.drawPanel;
    }

    public void drawPanel(Graphics g) {
        this.drawPanel.draw(g);
    }

    public void drawHover(Graphics g) {
        if ((hover || forceHover) && active && hoverAction != null) {
            hoverAction.draw(g, this);
        }
    }

    public void update(boolean isSelected, ControlKey... optionalKeys) {
        if (!active) {
            return;
        }

        hover = false;
        press = false;

        InputControl input = InputControl.instance();
        if (input.isMouseInput()) {
            Point mouseLocation = input.getMouseLocation();

            int mx = mouseLocation.x;
            int my = mouseLocation.y;

            if (mx >= x && my >= y && mx <= x + width && my <= y + height) {
                hover = true;
                if (input.consumeIfMouseDown()) {
                    press = true;
                }
            }
        }

        if (isSelected && input.consumeIfDown(ControlKey.SPACE)) {
            press = true;
        }

        for (ControlKey c : optionalKeys) {
            if (input.consumeIfDown(c)) {
                press = true;
            }
        }
    }

    public boolean checkConsumePress() {
        if (press) {
            press = false;
            pressAction.buttonPressed();
            return true;
        }

        return false;
    }

    public boolean isActive() {
        return active;
    }

    // Sets active to input, and skips panel draw if not active
    public void setActiveSkip(boolean active) {
        this.setActive(active);
        this.panel().setSkip(!active);
    }

    public void setActive(boolean set) {
        active = set;
        if (!active) {
            hover = false;
            setForceHover(false);
        }
    }

    public void setForceHover(boolean set) {
        forceHover = set;
    }

    public boolean isHover() {
        return this.hover;
    }

    public boolean isForceHover() {
        return this.forceHover;
    }

    public boolean isPress() {
        return this.press;
    }

    public int nextTransition(Direction direction) {
        return this.transitions[direction.ordinal()];
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
