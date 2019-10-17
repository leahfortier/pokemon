package draw.button;

import draw.PolygonUtils;
import draw.panel.DrawPanel;
import map.Direction;
import util.string.StringUtils;

import java.awt.Graphics;

public class ButtonPanel extends DrawPanel {
    private final Button button;

    private String label;
    private int fontSize;

    private Direction arrowDirection;

    private boolean greyInactive;

    // Should only be created from Button constructor
    ButtonPanel(Button button) {
        super(button);

        this.button = button;

        // By default, button has a black outline and no background or border
        this.withBlackOutline();
        this.withBackgroundColor(null);
        this.withBorderPercentage(0);
    }

    public Button button() {
        return this.button;
    }

    public ButtonPanel greyInactive() {
        this.greyInactive = true;
        return this;
    }

    // Sets the arrow direction and removes the black outline
    public ButtonPanel asArrow(Direction arrowDirection) {
        this.arrowDirection = arrowDirection;
        return this.withNoOutline().asButtonPanel();
    }

    public ButtonPanel withLabel(String text, int fontSize) {
        this.label = text;
        this.fontSize = fontSize;
        return this;
    }

    public void draw(Graphics g) {
        // If button is inactive, set greyOut to false
        // Note: This is not actually drawing the grey out, just setting it (will be drawn in drawBackground)
        if (this.greyInactive) {
            super.greyOut(!button.isActive());
        }

        super.drawBackground(g);

        // If label was provided, draw it
        if (!StringUtils.isNullOrEmpty(label)) {
            super.label(g, fontSize, label);
        }

        // Arrow buttons!
        if (this.arrowDirection != null) {
            PolygonUtils.drawArrow(g, x, y, width, height, arrowDirection);
        }
    }
}
