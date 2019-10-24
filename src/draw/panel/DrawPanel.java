package draw.panel;

import draw.Alignment;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.PolygonUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonPanel;
import draw.button.ButtonPanel.ButtonPanelSetup;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import main.Global;
import map.Direction;
import pokemon.active.PartyPokemon;
import type.PokeType;
import util.FontMetrics;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.List;

public class DrawPanel implements Panel {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    private int borderSize;

    private Color backgroundColor;
    private Color borderColor;

    private Color secondBackgroundColor;
    private boolean swapDualColoredDimensions;

    private Direction[] outlineDirections;

    private boolean transparentBackground;
    private boolean onlyTransparency;
    private int transparentCount;

    private boolean greyOut;
    private boolean highlight;
    private Color highlightColor;

    protected boolean skipDraw;

    private int fontSize;
    private String label;
    private Color labelColor;
    private Alignment labelAlignment;

    private BufferedImage imageLabel;
    private BufferedImage bottomRightImage;

    public DrawPanel(Button button) {
        this(button.x, button.y, button.width, button.height);
    }

    public DrawPanel(int x, int y, Point dimension) {
        this(x, y, dimension.x, dimension.y);
    }

    public DrawPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.withBackgroundColor(Color.WHITE)
            .withBorderColor(Color.LIGHT_GRAY)
            .withBorderPercentage(10);

        this.withNoOutline();

        this.withLabelSize(30)
            .withLabelColor(Color.BLACK);

        // Note: This does not make the panel transparent by default
        // It's just the default count if transparency is set
        this.transparentCount = 1;
    }

    public DrawPanel withBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public DrawPanel withBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public DrawPanel withBackgroundColors(Color[] backgroundColors) {
        return this.withBackgroundColors(backgroundColors, false);
    }

    public DrawPanel withBackgroundColors(Color[] backgroundColors, boolean swapDualColoredDimensions) {
        this.backgroundColor = backgroundColors[0];
        this.secondBackgroundColor = backgroundColors[1];
        this.swapDualColoredDimensions = swapDualColoredDimensions;
        return this;
    }

    public DrawPanel withTypeColors(PartyPokemon pokemon) {
        return this.withBackgroundColors(PokeType.getColors(pokemon));
    }

    public DrawPanel withTransparentCount(int transparentCount) {
        this.transparentCount = transparentCount;
        return this.withTransparentBackground();
    }

    public DrawPanel withFullTransparency() {
        this.onlyTransparency = true;
        return this.withTransparentBackground().withBorderPercentage(0);
    }

    public DrawPanel withBorderlessTransparentBackground() {
        return this.withTransparentBackground().withBorderPercentage(0);
    }

    public DrawPanel withTransparentBackground() {
        this.transparentBackground = true;
        return this;
    }

    public DrawPanel withTransparentBackground(Color backgroundColor) {
        return this.withTransparentBackground().withBackgroundColor(backgroundColor);
    }

    public DrawPanel withBorderPercentage(int borderPercentage) {
        return this.withBorderSize((int)(borderPercentage/100.0*Math.min(width, height)));
    }

    public DrawPanel withBorderSize(int borderSize) {
        this.borderSize = borderSize;
        return this;
    }

    // Basically invisible (generally used for spacing)
    public DrawPanel withNoBackground() {
        return this.withBackgroundColor(null)
                   .withBorderPercentage(0)
                   .withNoOutline();
    }

    public DrawPanel withNoOutline() {
        this.outlineDirections = new Direction[0];
        return this;
    }

    public DrawPanel withBlackOutline() {
        this.outlineDirections = Direction.values();
        return this;
    }

    public DrawPanel withOutlines(List<Direction> outlineDirections) {
        this.outlineDirections = outlineDirections.toArray(new Direction[0]);
        return this;
    }

    public DrawPanel withConditionalOutline(boolean shouldOutline) {
        if (shouldOutline) {
            return this.withBlackOutline();
        } else {
            return this.withNoOutline();
        }
    }

    // Gives a black outline for every direction other than the input missingBlackOutline
    public DrawPanel withMissingBlackOutline(Direction missingBlackOutline) {
        this.outlineDirections = EnumSet.complementOf(EnumSet.of(missingBlackOutline)).toArray(new Direction[0]);
        return this;
    }

    public DrawPanel withLabelSize(int fontSize) {
        return this.withLabelSize(fontSize, Alignment.CENTER);
    }

    public DrawPanel withLabelSize(int fontSize, Alignment alignment) {
        this.fontSize = fontSize;
        this.labelAlignment = alignment;
        return this;
    }

    public DrawPanel withLabelColor(Color labelColor) {
        this.labelColor = labelColor;
        return this;
    }

    public DrawPanel withLabel(String text, int fontSize) {
        return this.withLabel(text, fontSize, Alignment.CENTER);
    }

    public DrawPanel withLabel(String text, int fontSize, Alignment alignment) {
        return this.withLabelSize(fontSize, alignment).withLabel(text);
    }

    // Sets the label and assumes no image will be drawn
    // Use withImageLabel to indicate both should be drawn together
    public DrawPanel withLabel(String text) {
        return this.withImageLabel(null, text);
    }

    // Sets the label to only be this image and will remove any text label already set
    // Use method which takes in both image and label to indicate both should be drawn together
    public DrawPanel withImageLabel(BufferedImage image) {
        return this.withImageLabel(image, null);
    }

    public DrawPanel withImageLabel(BufferedImage image, String label) {
        this.imageLabel = image;
        this.label = label;
        return this;
    }

    public DrawPanel withBottomRightImage(BufferedImage image) {
        this.bottomRightImage = image;
        return this;
    }

    public DrawPanel withGreyOut(boolean greyOut) {
        this.greyOut = greyOut;
        return this;
    }

    // Must have a background color set and will highlight that color when true
    public DrawPanel withHighlight(boolean shouldHighlight, Color highlightColor) {
        this.highlight = shouldHighlight;
        this.highlightColor = highlightColor;
        return this;
    }

    public void skipDraw(boolean shouldSkip) {
        this.skipDraw = shouldSkip;
    }

    public void skipDraw() {
        this.skipDraw(true);
    }

    public WrapPanel asWrapPanel() {
        if (this instanceof WrapPanel) {
            return (WrapPanel)this;
        }

        Global.error("Must already be a WrapPanel.");
        return new WrapPanel(x, y, width, height, 0);
    }

    public MovePanel asMovePanel() {
        if (this instanceof MovePanel) {
            return (MovePanel)this;
        }

        Global.error("Must already be a MovePanel.");
        return new MovePanel(this, 0, 0, 0);
    }

    public ButtonPanel asButtonPanel() {
        if (this instanceof ButtonPanel) {
            return (ButtonPanel)this;
        }

        Global.error("Must already be a ButtonPanel.");
        return new Button(this, null).panel();
    }

    public int getBorderSize() {
        if (onlyTransparency) {
            return 0;
        }

        return this.borderSize;
    }

    public void fillBar(Graphics g, Color color, double ratio) {
        ratio = Math.min(1, ratio);

        fillTransparent(g);

        g.setColor(color);
        g.fillRect(x, y, (int)(ratio*width), height);

        blackOutline(g);
    }

    public void fillTransparent(Graphics g) {
        DrawUtils.fillTransparent(g, x, y, width, height);
    }

    public void blackOutline(Graphics g) {
        DrawUtils.blackOutline(g, x, y, width, height, outlineDirections);
    }

    private void fill(Graphics g, Color color) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }

    // Add a sad dark-red filter over everything
    public void faintOut(Graphics g) {
        this.fill(g, new Color(190, 49, 46, 64));
    }

    private void drawLabel(Graphics g) {
        FontMetrics.setFont(g, fontSize, labelColor);
        switch (this.labelAlignment) {
            case LEFT:
                this.drawLeftLabel(g, fontSize, label);
                break;
            case RIGHT:
                this.drawRightLabel(g, fontSize, label);
                break;
            default:
                this.label(g, fontSize, label);
                break;
        }
    }

    private void drawImageLabel(Graphics g) {
        FontMetrics.setBlackFont(g, fontSize);
        switch (this.labelAlignment) {
            case LEFT:
                this.leftImageLabel(g, fontSize, imageLabel, label);
                break;
            case RIGHT:
                Global.error("Right image label is currently unsupported");
                break;
            default:
                this.imageLabel(g, fontSize, imageLabel, label);
                break;
        }
    }

    public void draw(Graphics g) {
        if (this.skipDraw) {
            this.skipDraw = false;
            return;
        }

        this.drawBackground(g);

        FontMetrics.setFont(g, this.fontSize);

        // Labels (both text and images)
        if (this.label != null && this.imageLabel != null) {
            // Both image and text -- center together
            this.drawImageLabel(g);
        } else if (label != null) {
            // Only text
            this.drawLabel(g);
        } else if (this.imageLabel != null) {
            // Only image
            this.imageLabel(g, imageLabel);
        }

        // Image in the bottom right of the panel
        if (this.bottomRightImage != null) {
            ImageUtils.drawBottomRightImage(g, bottomRightImage, this.rightX(), this.bottomY());
        }
    }

    public void drawBackground(Graphics g) {
        // If not full transparency, draw the background colors
        if (!onlyTransparency && backgroundColor != null) {
            if (secondBackgroundColor == null) {
                g.setColor(backgroundColor);
                g.fillRect(x, y, width, height);
            } else {
                PolygonUtils.drawDualColoredBackground(g, x, y, width, height, backgroundColor, secondBackgroundColor, swapDualColoredDimensions);
            }
        }

        if (greyOut) {
            DrawUtils.greyOut(g, x, y, width, height);
            greyOut = false;
        } else if (highlight) {
            this.fill(g, highlightColor.darker());
            highlight = false;
        }

        int borderSize = this.getBorderSize();
        if (transparentBackground) {
            for (int i = 0; i < transparentCount; i++) {
                DrawUtils.fillTransparent(g, x + borderSize, y + borderSize, width - 2*borderSize, height - 2*borderSize);
            }
        } else {
            DrawUtils.drawBorder(g, borderColor, x, y, width, height, borderSize);
        }

        blackOutline(g);
    }

    public Button[] getButtons(int spacing, int numRows, int numCols) {
        return this.getButtons(spacing, numRows, numCols, 0, null, null);
    }

    public Button[] getButtons(int spacing, int numRows, int numCols, int startIndex, ButtonTransitions defaultTransitions) {
        return this.getButtons(spacing, numRows, numCols, numRows, numCols, startIndex, defaultTransitions, null);
    }

    public Button[] getButtons(int spacing, int numRows, int numCols, int startIndex, ButtonTransitions defaultTransitions, ButtonIndexAction indexAction) {
        return this.getButtons(spacing, numRows, numCols, startIndex, defaultTransitions, indexAction, null);
    }

    public Button[] getButtons(int spacing, int numRows, int numCols, int startIndex,
                               ButtonTransitions defaultTransitions, ButtonIndexAction indexAction, PanelIndexSetup indexSetup) {
        return this.getButtons(spacing, numRows, numCols, numRows, numCols, startIndex, defaultTransitions, indexAction, indexSetup);
    }

    public Button[] getButtons(int spacing, int numSpaceRows, int numSpaceCols, int numButtonRows, int numButtonCols, int startIndex,
                               ButtonTransitions defaultTransitions, ButtonIndexAction indexAction) {
        return this.getButtons(spacing, numSpaceRows, numSpaceCols, numButtonRows, numButtonCols, startIndex, defaultTransitions, indexAction, null);
    }

    public Button[] getButtons(int spacing, int numSpaceRows, int numSpaceCols, int numButtonRows, int numButtonCols, int startIndex,
                               ButtonTransitions defaultTransitions, ButtonIndexAction indexAction, PanelIndexSetup indexSetup) {
        int buttonWidth = (this.width - (numSpaceCols + 1)*spacing)/numSpaceCols;
        int buttonHeight = (this.height - (numSpaceRows + 1)*spacing)/numSpaceRows;

        return this.getButtons(buttonWidth, buttonHeight, numSpaceRows, numSpaceCols, numButtonRows, numButtonCols, startIndex, defaultTransitions, indexAction, indexSetup);
    }

    public Button[] getButtons(int buttonWidth, int buttonHeight, int numRows, int numCols, ButtonIndexAction indexAction) {
        return this.getButtons(buttonWidth, buttonHeight, numRows, numCols, numRows, numCols, 0, null, indexAction);
    }

    public Button[] getButtons(int buttonWidth, int buttonHeight, int numRows, int numCols) {
        return this.getButtons(buttonWidth, buttonHeight, numRows, numCols, numRows, numCols, 0, null, null);
    }

    public Button[] getButtons(int buttonWidth, int buttonHeight, int numButtonRows, int numButtonCols, int startValue,
                               ButtonTransitions defaultTransitions, ButtonIndexAction indexAction, PanelIndexSetup indexSetup) {
        return this.getButtons(buttonWidth, buttonHeight,
                               numButtonRows, numButtonCols,
                               numButtonRows, numButtonCols,
                               startValue,
                               defaultTransitions, indexAction, indexSetup
        );
    }

    public Button[] getButtons(int buttonWidth, int buttonHeight, int numSpaceRows, int numSpaceCols,
                               int numButtonRows, int numButtonCols, int startValue,
                               ButtonTransitions defaultTransitions, ButtonIndexAction indexAction) {
        return this.getButtons(buttonWidth, buttonHeight,
                               numSpaceRows, numSpaceCols,
                               numButtonRows, numButtonCols,
                               startValue,
                               defaultTransitions, indexAction, null
        );
    }

    public Button[] getButtons(
            int buttonWidth, int buttonHeight,
            int numSpaceRows, int numSpaceCols,
            int numButtonRows, int numButtonCols,
            int startValue,
            ButtonTransitions defaultTransitions,
            ButtonIndexAction indexAction,
            PanelIndexSetup indexSetup) {
        int borderSize = this.getBorderSize();

        int horizontalSpacing = this.width - 2*borderSize - numSpaceCols*buttonWidth;
        int verticalSpacing = this.height - 2*borderSize - numSpaceRows*buttonHeight;

        int xSpacing = horizontalSpacing/(numSpaceCols + 1);
        int ySpacing = verticalSpacing/(numSpaceRows + 1);

        Button[] buttons = new Button[numButtonRows*numButtonCols];
        for (int row = 0, index = 0; row < numButtonRows; row++) {
            for (int col = 0; col < numButtonCols; col++, index++) {
                // Setup default transitions
                ButtonTransitions transitions = ButtonTransitions.getBasicTransitions(
                        index, numButtonRows, numButtonCols, startValue, defaultTransitions
                );

                // Create the button with all them specs
                buttons[index] = new Button(
                        this.x + borderSize + xSpacing*(col + 1) + buttonWidth*col,
                        this.y + borderSize + ySpacing*(row + 1) + buttonHeight*row,
                        buttonWidth,
                        buttonHeight,
                        transitions,
                        ButtonIndexAction.getPressAction(indexAction, index),
                        PanelIndexSetup.getPanelSetup(indexSetup, index)
                );
            }
        }

        return buttons;
    }

    // TODO: This looks like it's more for vertical space but the name sounds like horizontal
    // Gonna use getSpace for now but should come back to this to see if it can be deprecated/more clear/reworked/etc
    public int getTextSpace(Graphics g) {
        return this.getBorderSize() + FontMetrics.getDistanceBetweenRows(g) - FontMetrics.getTextHeight(g);
    }

    public int getSpace(Graphics g) {
        return this.getBorderSize() + FontMetrics.getTextWidth(g);
    }

    public void drawLeftLabel(Graphics g, int fontSize, String label) {
        int startX = x + this.getTextSpace(g);
        int centerY = centerY();

        FontMetrics.setFont(g, fontSize);
        TextUtils.drawCenteredHeightString(g, label, startX, centerY);
    }

    public void drawRightLabel(Graphics g, int fontSize, String label) {
        int startX = rightX() - this.getTextSpace(g);
        int centerY = centerY();

        FontMetrics.setFont(g, fontSize);
        TextUtils.drawCenteredHeightString(g, label, startX, centerY, Alignment.RIGHT);
    }

    public void imageLabel(Graphics g, BufferedImage image) {
        ImageUtils.drawCenteredImage(g, image, centerX(), centerY());
    }

    public void imageLabel(Graphics g, int fontSize, BufferedImage image, String label) {
        FontMetrics.setFont(g, fontSize);
        ImageUtils.drawCenteredImageLabel(g, image, label, centerX(), centerY());
    }

    // Spacing is kind of specific for the bag tabs right now and not sure how bad that is without another sample
    public void leftImageLabel(Graphics g, int fontSize, BufferedImage image, String label) {
        int spacing = FontMetrics.getTextWidth(g)/3;
        int startX = x + this.getBorderSize() + spacing;

        FontMetrics.setFont(g, fontSize);
        ImageUtils.drawCenteredHeightImageLabel(g, image, label, startX, centerY(), spacing);
    }

    @Override
    public int centerX() {
        return x + width/2;
    }

    @Override
    public int centerY() {
        return y + height/2;
    }

    @Override
    public int rightX() {
        return x + width;
    }

    @Override
    public int bottomY() {
        return y + height;
    }

    public void label(Graphics g, String text) {
        label(g, g.getFont().getSize(), text);
    }

    public void label(Graphics g, int fontSize, String text) {
        this.label(g, fontSize, labelColor, text);
    }

    public void label(Graphics g, int fontSize, Color color, String text) {
        g.setColor(color);
        FontMetrics.setFont(g, fontSize);
        TextUtils.drawCenteredString(g, text, x, y, width, height);
    }

    public DrawPanel createBottomTab(int tabIndex, int tabHeight, int numTabs) {
        return this.createTab(tabIndex, tabHeight, numTabs, false, true);
    }

    public DrawPanel createBottomInsetTab(int tabIndex, int tabHeight, int numTabs) {
        return this.createTab(tabIndex, tabHeight, numTabs, true, true);
    }

    public DrawPanel createTab(int tabIndex, int tabHeight, int numTabs) {
        return this.createTab(tabIndex, tabHeight, numTabs, false, false);
    }

    // Inset is true if the button should overlap with the panel
    private DrawPanel createTab(int tabIndex, int tabHeight, int numTabs, boolean inset, boolean isBottomTab) {
        int tabWidth = this.width/numTabs;
        int remainder = this.width%numTabs;

        int offset = inset ? tabHeight - DrawUtils.OUTLINE_SIZE : 0;
        int y = isBottomTab ? this.bottomY() - DrawUtils.OUTLINE_SIZE - offset
                            : this.y - tabHeight + DrawUtils.OUTLINE_SIZE + offset;

        return new DrawPanel(
                this.x + tabIndex*tabWidth + Math.min(tabIndex, remainder),
                y,
                tabWidth + (tabIndex < remainder ? 1 : 0),
                tabHeight
        );
    }

    @FunctionalInterface
    public interface ButtonIndexAction {
        void pressButton(int index);

        static ButtonPressAction getPressAction(ButtonIndexAction indexAction, int index) {
            return indexAction == null
                   ? () -> {}
                   : () -> indexAction.pressButton(index);
        }
    }

    @FunctionalInterface
    public interface PanelIndexSetup {
        void setup(int index, ButtonPanel panel);

        static ButtonPanelSetup getPanelSetup(PanelIndexSetup indexSetup, int index) {
            return indexSetup == null
                   ? panel -> {}
                   : panel -> indexSetup.setup(index, panel);
        }

        static PanelIndexSetup add(PanelIndexSetup base, PanelIndexSetup addition) {
            if (base == null && addition == null) {
                return (index, panel) -> {};
            } else if (base == null) {
                return addition;
            } else if (addition == null) {
                return base;
            }

            return (index, panel) -> {
                base.setup(index, panel);
                addition.setup(index, panel);
            };
        }
    }
}
