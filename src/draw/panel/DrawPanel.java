package draw.panel;

import draw.Alignment;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.PolygonUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
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

public class DrawPanel {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    private int borderPercentage;

    private Color backgroundColor;
    private Color borderColor;

    private Color secondBackgroundColor;
    private boolean swapDualColoredDimensions;

    private Direction[] outlineDirections;

    private boolean transparentBackground;
    private boolean greyOut;

    private boolean onlyTransparency;
    private int transparentCount;

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

        this.borderPercentage = 10;

        this.backgroundColor = Color.WHITE;
        this.borderColor = Color.LIGHT_GRAY;

        this.outlineDirections = new Direction[0];

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

    public DrawPanel withTransparentBackground() {
        this.transparentBackground = true;
        return this;
    }

    public DrawPanel withTransparentBackground(Color backgroundColor) {
        return this.withTransparentBackground().withBackgroundColor(backgroundColor);
    }

    public DrawPanel withBorderPercentage(int borderPercentage) {
        this.borderPercentage = borderPercentage;
        return this;
    }

    public DrawPanel withBlackOutline() {
        this.outlineDirections = Direction.values();
        return this;
    }

    // Gives a black outline for every direction other than the input missingBlackOutline
    public DrawPanel withMissingBlackOutline(Direction missingBlackOutline) {
        this.outlineDirections = EnumSet.complementOf(EnumSet.of(missingBlackOutline)).toArray(new Direction[0]);
        return this;
    }

    public DrawPanel greyOut() {
        this.greyOut = true;
        return this;
    }

    public WrapPanel asWrapPanel() {
        if (this instanceof WrapPanel) {
            return (WrapPanel)this;
        }

        Global.error("Must already be a WrapPanel.");
        return new WrapPanel(x, y, width, height, 16);
    }

    public MovePanel asMovePanel() {
        if (this instanceof MovePanel) {
            return (MovePanel)this;
        }

        Global.error("Must already be a MovePanel.");
        return new MovePanel(this, 0, 0, 0);
    }

    public int getBorderSize() {
        if (onlyTransparency) {
            return 0;
        }

        return (int)(borderPercentage/100.0*Math.min(width, height));
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
        return this.getButtons(spacing, numRows, numCols, numRows, numCols, startIndex, defaultTransitions, indexAction);
    }

    public Button[] getButtons(int spacing, int numSpaceRows, int numSpaceCols, int numButtonRows, int numButtonCols, int startIndex, ButtonTransitions defaultTransitions, ButtonIndexAction indexAction) {
        int buttonWidth = (this.width - (numSpaceCols + 1)*spacing)/numSpaceCols;
        int buttonHeight = (this.height - (numSpaceRows + 1)*spacing)/numSpaceRows;

        return this.getButtons(buttonWidth, buttonHeight, numSpaceRows, numSpaceCols, numButtonRows, numButtonCols, startIndex, defaultTransitions, indexAction);
    }

    public Button[] getButtons(int buttonWidth, int buttonHeight, int numRows, int numCols, ButtonIndexAction indexAction) {
        return this.getButtons(buttonWidth, buttonHeight, numRows, numCols, numRows, numCols, 0, null, indexAction);
    }

    public Button[] getButtons(int buttonWidth, int buttonHeight, int numRows, int numCols) {
        return this.getButtons(buttonWidth, buttonHeight, numRows, numCols, numRows, numCols, 0, null, null);
    }

    public Button[] getButtons(
            int buttonWidth, int buttonHeight,
            int numSpaceRows, int numSpaceCols,
            int numButtonRows, int numButtonCols,
            int startValue,
            ButtonTransitions defaultTransitions,
            ButtonIndexAction indexAction) {
        int borderSize = this.getBorderSize();

        int horizontalSpacing = this.width - 2*borderSize - numSpaceCols*buttonWidth;
        int verticalSpacing = this.height - 2*borderSize - numSpaceRows*buttonHeight;

        int xSpacing = horizontalSpacing/(numSpaceCols + 1);
        int ySpacing = verticalSpacing/(numSpaceRows + 1);

        Button[] buttons = new Button[numButtonRows*numButtonCols];
        for (int row = 0, index = 0; row < numButtonRows; row++) {
            for (int col = 0; col < numButtonCols; col++, index++) {
                final int finalIndex = index;

                buttons[index] = new Button(
                        this.x + borderSize + xSpacing*(col + 1) + buttonWidth*col,
                        this.y + borderSize + ySpacing*(row + 1) + buttonHeight*row,
                        buttonWidth,
                        buttonHeight,
                        ButtonHoverAction.BOX,
                        ButtonTransitions.getBasicTransitions(index, numButtonRows, numButtonCols, startValue, defaultTransitions),
                        indexAction == null ? () -> {} : () -> indexAction.pressButton(finalIndex)
                );
            }
        }

        return buttons;
    }

    public int getTextSpace(Graphics g) {
        return this.getBorderSize() + FontMetrics.getDistanceBetweenRows(g) - FontMetrics.getTextHeight(g);
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

    public int centerX() {
        return x + width/2;
    }

    public int centerY() {
        return y + height/2;
    }

    public int rightX() {
        return x + width;
    }

    public int bottomY() {
        return y + height;
    }

    public void label(Graphics g, String text) {
        label(g, g.getFont().getSize(), text);
    }

    public void label(Graphics g, int fontSize, String text) {
        g.setColor(Color.BLACK);
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
        int y = isBottomTab ? this.y + this.height - DrawUtils.OUTLINE_SIZE - offset
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
    }
}
