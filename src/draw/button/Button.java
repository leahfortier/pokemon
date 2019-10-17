package draw.button;

import battle.attack.Move;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.PolygonUtils;
import draw.TextUtils;
import draw.panel.DrawPanel;
import input.ControlKey;
import input.InputControl;
import map.Direction;
import util.FontMetrics;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Button {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    private final ButtonHoverAction hoverAction;
    private final ButtonPressAction pressAction;
    private final int[] transitions;

    private boolean hover;
    private boolean press;
    private boolean forceHover;
    private boolean active;

    public Button(int x, int y, int width, int height) {
        this(x, y, width, height, null, null, null);
    }

    public Button(int x, int y, int width, int height, ButtonHoverAction hoverAction, ButtonTransitions transitions) {
        this(x, y, width, height, hoverAction, transitions, null);
    }

    public Button(DrawPanel panel, ButtonTransitions transitions) {
        this(panel, transitions, null);
    }

    public Button(DrawPanel panel, ButtonTransitions transitions, ButtonPressAction pressAction) {
        this(panel.x, panel.y, panel.width, panel.height, ButtonHoverAction.BOX, transitions, pressAction);
    }

    public Button(int x, int y, int width, int height, ButtonHoverAction hoverAction, ButtonTransitions transitions, ButtonPressAction pressAction) {
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

        this.hover = false;
        this.press = false;
        this.forceHover = false;
        this.active = true;
    }

    public void draw(Graphics g) {
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

    // Generally used for inactive buttons
    public void greyOut(Graphics g) {
        DrawUtils.greyOut(g, x, y, width, height);
    }

    // Generally used for currently selected buttons
    public void highlight(Graphics g, Color buttonColor) {
        this.fill(g, buttonColor.darker());
    }

    public void fillTranslated(Graphics g, Color color) {
        fill(g, color, 0, 0);
    }

    public void fill(Graphics g, Color color) {
        fill(g, color, x, y);
    }

    private void fill(Graphics g, Color color, int x, int y) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }

    public void fillBordered(Graphics g, Color color) {
        new DrawPanel(x, y, width, height)
                .withTransparentBackground(color)
                .withTransparentCount(2)
                .withBorderPercentage(15)
                .withBlackOutline()
                .drawBackground(g);
    }

    // Fills transparent, outlines in black, and draws the label centered
    public void fillBorderLabel(Graphics g, int fontSize, String label) {
        fillBorderLabel(g, null, fontSize, label);
    }

    // Fills color transparent, outlines in black, and draws the label centered
    // Color may be null, but should really use the other method for that
    public void fillBorderLabel(Graphics g, Color color, int fontSize, String label) {
        fillTransparent(g, color);
        blackOutline(g);
        label(g, fontSize, label);
    }

    public void fillTransparent(Graphics g, Color color) {
        if (color != null) {
            fill(g, color);
        }
        fillTransparent(g);
    }

    public void fillTransparent(Graphics g) {
        DrawUtils.fillTransparent(g, x, y, width, height);
    }

    public void blackOutline(Graphics g, Direction... directions) {
        DrawUtils.blackOutline(g, x, y, width, height, directions);
    }

    public void outlineTab(Graphics g, int index, int selectedIndex) {
        List<Direction> toOutline = new ArrayList<>();
        toOutline.add(Direction.UP);
        toOutline.add(Direction.RIGHT);

        if (index == 0) {
            toOutline.add(Direction.LEFT);
        }

        if (index != selectedIndex) {
            toOutline.add(Direction.DOWN);
        }

        this.blackOutline(g, toOutline.toArray(new Direction[0]));
    }

    public void label(Graphics g, int fontSize, String text) {
        label(g, fontSize, Color.BLACK, text);
    }

    public void label(Graphics g, int fontSize, Color color, String text) {
        g.setColor(color);
        FontMetrics.setFont(g, fontSize);
        TextUtils.drawCenteredString(g, text, x, y, width, height);
    }

    public void imageLabel(Graphics g, BufferedImage image) {
        ImageUtils.drawCenteredImage(g, image, centerX(), centerY());
    }

    public int rightX() {
        return x + width;
    }

    public int bottomY() {
        return y + height;
    }

    public int centerX() {
        return x + width/2;
    }

    public int centerY() {
        return y + height/2;
    }

    public void drawArrow(Graphics g, Direction direction) {
        PolygonUtils.drawArrow(g, x, y, width, height, direction);
    }

    public void drawMoveButton(Graphics g, Move move) {
        g.translate(x, y);

        new DrawPanel(0, 0, width, height)
                .withTransparentBackground(move.getAttack().getActualType().getColor())
                .withBorderPercentage(15)
                .withBlackOutline()
                .drawBackground(g);

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 22);
        g.drawString(move.getAttack().getName(), 10, 26);

        FontMetrics.setFont(g, 18);
        TextUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 170, 45);

        g.translate(-x, -y);

        this.draw(g);
    }
}
