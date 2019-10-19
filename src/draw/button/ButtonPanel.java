package draw.button;

import draw.PolygonUtils;
import draw.panel.DrawPanel;
import map.Direction;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class ButtonPanel extends DrawPanel {
    private final Button button;

    // For arrow buttons (width and height default to button size, but can be specified otherwise
    private Direction arrowDirection;
    private int arrowWidth;
    private int arrowHeight;

    private boolean onlyActiveDraw;
    private boolean greyInactive;

    private boolean skipDraw;

    // Should only be created from Button constructor
    ButtonPanel(Button button, ButtonPanelSetup setup) {
        super(button);

        this.button = button;

        // By default, button has a black outline and no background or border
        this.withBlackOutline();
        this.withBackgroundColor(null);
        this.withBorderPercentage(0);

        if (setup != null) {
            setup.setup(this);
        }
    }

    public Button button() {
        return this.button;
    }

    public ButtonPanel onlyActiveDraw() {
        this.onlyActiveDraw = true;
        return this;
    }

    public ButtonPanel greyInactive() {
        this.greyInactive = true;
        return this;
    }

    public void skipDraw() {
        this.skipDraw = true;
    }

    // Sets the arrow direction and removes the black outline
    public ButtonPanel asArrow(Direction arrowDirection) {
        return this.asArrow(arrowDirection, width, height)
                   .withNoOutline()
                   .asButtonPanel();
    }

    public ButtonPanel asArrow(Direction arrowDirection, int arrowWidth, int arrowHeight) {
        this.arrowDirection = arrowDirection;
        this.arrowWidth = arrowWidth;
        this.arrowHeight = arrowHeight;
        return this;
    }

    public ButtonPanel withTabOutlines(int index, int selectedIndex) {
        List<Direction> toOutline = new ArrayList<>();
        toOutline.add(Direction.UP);
        toOutline.add(Direction.RIGHT);

        if (index == 0) {
            toOutline.add(Direction.LEFT);
        }

        if (index != selectedIndex) {
            toOutline.add(Direction.DOWN);
        }

        return this.withOutlines(toOutline).asButtonPanel();
    }

    @Override
    public void draw(Graphics g) {
        if (this.skipDraw) {
            this.skipDraw = false;
            return;
        }

        if (!button.isActive()) {
            if (this.onlyActiveDraw) {
                // If only draw when active, and is not active, skip the current draw
                return;
            } else if (this.greyInactive) {
                // If button is inactive, set greyOut to true
                // Note: This is not actually drawing the grey out, just setting it (will be drawn in drawBackground)
                super.setGreyOut();
            }
        }

        super.draw(g);

        // Arrow buttons!
        if (this.arrowDirection != null) {
            PolygonUtils.drawCenteredArrow(g, centerX(), centerY(), arrowWidth, arrowHeight, arrowDirection);
        }
    }

    @FunctionalInterface
    public interface ButtonPanelSetup {
        void setup(ButtonPanel panel);
    }
}
