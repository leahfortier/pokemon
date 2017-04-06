package draw.panel;

import battle.attack.Attack;
import draw.Alignment;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.PolygonUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import input.ControlKey;
import input.InputControl;
import main.Global;
import map.Direction;
import util.FontMetrics;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Collection;

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

    private boolean animateMessage;

    public static DrawPanel fullGamePanel() {
        return new DrawPanel(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
    }

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

    public DrawPanel withTextAnimation() {
        this.animateMessage = true;
        return this;
    }

    public DrawPanel greyOut() {
        this.greyOut = true;
        return this;
    }

    public DrawPanel withBlackOutline(Collection<Direction> directions) {
        this.outlineDirections = directions.toArray(new Direction[0]);
        return this;
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
        return this.getButtons(spacing, numRows, numCols, 0, null);
    }

    public Button[] getButtons(int spacing, int numRows, int numCols, int startIndex, int[] defaultTransitions) {
        return this.getButtons(spacing, numRows, numCols, numRows, numCols, startIndex, defaultTransitions);
    }

    public Button[] getButtons(int spacing, int numSpaceRows, int numSpaceCols, int numButtonRows, int numButtonCols, int startIndex, int[] defaultTransitions) {
        int buttonWidth = (this.width - (numSpaceCols + 1)*spacing)/numSpaceCols;
        int buttonHeight = (this.height - (numSpaceRows + 1)*spacing)/numSpaceRows;

        return this.getButtons(buttonWidth, buttonHeight, numSpaceRows, numSpaceCols, numButtonRows, numButtonCols, startIndex, defaultTransitions);
    }

    public Button[] getButtons(int buttonWidth, int buttonHeight, int numRows, int numCols) {
        return this.getButtons(buttonWidth, buttonHeight, numRows, numCols, numRows, numCols, 0, null);
    }

    public Button[] getButtons(
            int buttonWidth, int buttonHeight,
            int numSpaceRows, int numSpaceCols,
            int numButtonRows, int numButtonCols,
            int startValue, int[] defaultTransitions) {
        int borderSize = this.getBorderSize();

        int horizontalSpacing = this.width - 2*borderSize - numSpaceCols*buttonWidth;
        int verticalSpacing = this.height - 2*borderSize - numSpaceRows*buttonHeight;

        int xSpacing = horizontalSpacing/(numSpaceCols + 1);
        int ySpacing = verticalSpacing/(numSpaceRows + 1);

        Button[] buttons = new Button[numButtonRows*numButtonCols];
        for (int row = 0, index = 0; row < numButtonRows; row++) {
            for (int col = 0; col < numButtonCols; col++, index++) {
                buttons[index] = new Button(
                        this.x + borderSize + xSpacing*(col + 1) + buttonWidth*col,
                        this.y + borderSize + ySpacing*(row + 1) + buttonHeight*row,
                        buttonWidth,
                        buttonHeight,
                        ButtonHoverAction.BOX,
                        Button.getBasicTransitions(index, numButtonRows, numButtonCols, startValue, defaultTransitions)
                );
            }
        }

        return buttons;
    }

    public int getTextSpace(Graphics g) {
        return this.getBorderSize() + FontMetrics.getDistanceBetweenRows(g) - FontMetrics.getTextHeight(g);
    }

    private int messageTimeElapsed = 0;
    private String drawingText;
    private boolean finishedAnimating;

    public int drawMessage(Graphics g, int fontSize, String text) {
        FontMetrics.setFont(g, fontSize);
        return drawMessage(g, text);
    }

    public int drawMessage(Graphics g, String text) {
        int startY = y + this.getTextSpace(g) + FontMetrics.getTextHeight(g);
        return drawMessage(g, text, startY);
    }

    public int drawMessage(Graphics g, String text, int startY) {
        g.setColor(Color.BLACK);

        int textSpace = this.getTextSpace(g);
        int startX = x + textSpace;
        int textWidth = width - 2*textSpace;

        if (!this.animateMessage) {
            return TextUtils.drawWrappedText(g, text, startX, startY, textWidth);
        }

        int lastWordLength;
        if (drawingText == null || !text.equals(drawingText)) {
            messageTimeElapsed = 0;
            drawingText = text;
            finishedAnimating = false;
        } else {
            messageTimeElapsed += 3 * Global.MS_BETWEEN_FRAMES;
        }

        int charactersToShow = finishedAnimating ? text.length() : Math.min(text.length(), messageTimeElapsed / 50);
        if (InputControl.instance().consumeIfMouseDown(ControlKey.SPACE)) {
            charactersToShow = text.length();
        }

        finishedAnimating = charactersToShow == text.length();

        if (charactersToShow != 0 && text.charAt(charactersToShow - 1) != ' '){
            String startString = text.substring(0, charactersToShow);
            int start = startString.lastIndexOf(' ') + 1;
            start = start == -1 ? 0 : start;

            String endString = text.substring(charactersToShow - 1);
            int end = endString.indexOf(' ');
            end = end == -1 ? endString.length() - 1 : end;

            lastWordLength = end - start + charactersToShow - 1;
        } else {
            lastWordLength = -1;
        }

        return TextUtils.drawWrappedText(g, text.substring(0, charactersToShow), lastWordLength, startX, startY, textWidth);
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
        ImageUtils.drawCenteredImage(g, image, x + width/2, y + height/2);
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

    public boolean isAnimatingMessage() {
        return !finishedAnimating && animateMessage;
    }

    public void drawMovePanel(Graphics g, Attack move) {
        this.withTransparentBackground(move.getActualType().getColor())
                .drawBackground(g);

        FontMetrics.setFont(g, 24);
        int spacing = 20;
        int y = this.y + spacing + FontMetrics.getTextHeight(g);
        g.drawString(move.getName(), this.x + spacing, y);

        BufferedImage typeImage = move.getActualType().getImage();
        int imageY = y - typeImage.getHeight();
        int imageX = this.rightX() - spacing - typeImage.getWidth();
        g.drawImage(typeImage, imageX, imageY, null);

        BufferedImage categoryImage = move.getCategory().getImage();
        imageX -= categoryImage.getWidth() + spacing;
        g.drawImage(categoryImage, imageX, imageY, null);

        y += FontMetrics.getDistanceBetweenRows(g);

        FontMetrics.setFont(g, 18);
        g.drawString("Power: " + move.getPowerString(), this.x + spacing, y);
        TextUtils.drawRightAlignedString(g, "Acc: " + move.getAccuracyString(), this.rightX() - spacing, y);

        y += FontMetrics.getDistanceBetweenRows(g) + 2;

        FontMetrics.setFont(g, 16);
        TextUtils.drawWrappedText(g,
                move.getDescription(),
                this.x + spacing,
                y,
                this.width - 2*spacing
        );
    }
}
