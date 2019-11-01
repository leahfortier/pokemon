package draw.button;

import battle.attack.Move;
import draw.PolygonUtils;
import draw.panel.DrawPanel;
import draw.panel.MoveButtonPanel;
import map.Direction;

import java.awt.Graphics;

public class ButtonPanel extends DrawPanel {
    private final Button button;

    private boolean skipInactive;
    private boolean greyInactive;

    // For arrow buttons (width and height default to button size, but can be specified otherwise
    private Direction arrowDirection;
    private int arrowWidth;
    private int arrowHeight;

    // For move buttons (displays name and PP)
    private MoveButtonPanel movePanel;

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

    public ButtonPanel skipInactive() {
        this.skipInactive = true;
        return this;
    }

    public ButtonPanel greyInactive() {
        this.greyInactive = true;
        return this;
    }

    // Sets the arrow direction and removes the black outline
    public ButtonPanel asArrow(Direction arrowDirection) {
        return (ButtonPanel)this.asArrow(arrowDirection, width, height)
                                .withNoOutline();
    }

    public ButtonPanel asArrow(Direction arrowDirection, int arrowWidth, int arrowHeight) {
        this.arrowDirection = arrowDirection;
        this.arrowWidth = arrowWidth;
        this.arrowHeight = arrowHeight;
        return this;
    }

    public ButtonPanel withBottomTabOutlines(int index, int selectedIndex) {
        return this.withTabOutlines(index, selectedIndex, true);
    }

    public ButtonPanel withTabOutlines(int index, int selectedIndex) {
        return this.withTabOutlines(index, selectedIndex, false);
    }

    private ButtonPanel withTabOutlines(int index, int selectedIndex, boolean isBottomTab) {
        if (index == selectedIndex) {
            this.withMissingBlackOutline(isBottomTab ? Direction.UP : Direction.DOWN);
        } else {
            this.withBlackOutline();
        }
        return this;
    }

    public ButtonPanel asMovePanel(int nameFontSize, int ppFontSize) {
        this.movePanel = new MoveButtonPanel(this, nameFontSize, ppFontSize);
        return this;
    }

    // Only works if asMovePanel has already been called
    public ButtonPanel withMove(Move move) {
        // Set the move so it knows what to draw
        this.movePanel.setMove(move);

        // Even though the border will not be drawn, it should use the same spacing as this panel
        this.movePanel.withBorderSize(this.getBorderSize());

        // Background will still be drawn on the current panel
        this.withBackgroundColor(move.getAttack().getActualType().getColor());
        return this;
    }

    @Override
    public void draw(Graphics g) {
        if (this.skipDraw) {
            this.skipDraw = false;
            return;
        }

        if (!button.isActive()) {
            if (this.skipInactive) {
                // If only draw when active, and is not active, skip the current draw
                return;
            } else if (this.greyInactive) {
                // If button is inactive, set greyOut to true
                // Note: This is not actually drawing the grey out, just setting it (will be drawn in drawBackground)
                super.withGreyOut(true);
            }
        }

        super.draw(g);

        // Arrow buttons!
        if (this.arrowDirection != null) {
            PolygonUtils.drawCenteredArrow(g, centerX(), centerY(), arrowWidth, arrowHeight, arrowDirection);
        }

        // Move button!
        if (movePanel != null) {
            movePanel.drawMove(g);
        }
    }

    @FunctionalInterface
    public interface ButtonPanelSetup {
        void setup(ButtonPanel panel);

        default ButtonPanelSetup add(ButtonPanelSetup next) {
            return panel -> {
                this.setup(panel);
                next.setup(panel);
            };
        }
    }

    @FunctionalInterface
    public interface ButtonPanelIndexSetup {
        void setup(ButtonPanel panel, int index);

        default ButtonPanelIndexSetup add(ButtonPanelIndexSetup next) {
            return (panel, index) -> {
                this.setup(panel, index);
                next.setup(panel, index);
            };
        }
    }
}
