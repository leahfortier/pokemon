package draw.panel;

import draw.DrawUtils;
import draw.TextWrapper;
import input.ControlKey;
import input.InputControl;
import main.Global;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;

public class WrapPanel extends DrawPanel {
    private final int fontSize;

    private boolean animateMessage;
    private int messageTimeElapsed;
    private String drawingText;
    private boolean finishedAnimating;

    public WrapPanel(int x, int y, int width, int height, int fontSize) {
        super(x, y, width, height);

        this.fontSize = fontSize;

        this.messageTimeElapsed = 0;
        this.finishedAnimating = true;
    }

    @Override
    public WrapPanel withBlackOutline() {
        return super.withBlackOutline().asWrapPanel();
    }

    @Override
    public WrapPanel withBackgroundColor(Color backgroundColor) {
        return super.withBackgroundColor(backgroundColor).asWrapPanel();
    }

    @Override
    public WrapPanel withBorderColor(Color borderColor) {
        return super.withBorderColor(borderColor).asWrapPanel();
    }

    @Override
    public WrapPanel withBorderPercentage(int borderPercentage) {
        return super.withBorderPercentage(borderPercentage).asWrapPanel();
    }

    @Override
    public WrapPanel withFullTransparency() {
        return super.withFullTransparency().asWrapPanel();
    }

    public WrapPanel withTextAnimation() {
        this.animateMessage = true;
        return this;
    }

    public boolean isAnimatingMessage() {
        return !finishedAnimating && animateMessage;
    }

    // Draws the text, wrapping to the next line if necessary and returns whether or not the text
    // fits entirely inside the panel
    public boolean drawMessage(Graphics g, String text) {
        FontMetrics.setFont(g, fontSize);
        g.setColor(Color.BLACK);

        // Include the outline size if there is no inset border
        // I realize not every panel is bordered, but it's probably fine for the ones that don't and is sometimes
        // necessary for that ones and having logic everywhere for different outline edges is overly complicated
        int borderSize = Math.max(this.getBorderSize(), DrawUtils.OUTLINE_SIZE);

        // Determine the maximum number of rows of text that could properly fit
        int height = this.height - 2*borderSize;
        int distanceBetweenRows = FontMetrics.getDistanceBetweenRows(g);
        int maxRows = height/distanceBetweenRows;

        int textHeight = FontMetrics.getTextHeight(g);
        int totalTextHeight = textHeight + distanceBetweenRows*(maxRows - 1);

        // Create spacing so that there is equal spacing on all sides when at the maximum number of rows
        int textSpace = (height - totalTextHeight)/2;
        int fullSpace = textSpace + borderSize;

        int startX = x + fullSpace;
        int startY = y + fullSpace + FontMetrics.getTextHeight(g);
        int textWidth = width - 2*fullSpace;
        int bottomY = this.bottomY() - borderSize;

        if (!this.animateMessage) {
            return new TextWrapper(g, text, startX, startY, textWidth).fits(bottomY);
        }

        if (!text.equals(drawingText)) {
            messageTimeElapsed = 0;
            drawingText = text;
            finishedAnimating = false;
        } else {
            messageTimeElapsed += 3*Global.MS_BETWEEN_FRAMES;
        }

        int charactersToShow = finishedAnimating ? text.length() : Math.min(text.length(), messageTimeElapsed/50);
        if (InputControl.instance().consumeIfMouseDown(ControlKey.SPACE)) {
            charactersToShow = text.length();
        }

        finishedAnimating = charactersToShow == text.length();

        int lastWordLength;
        if (charactersToShow != 0 && text.charAt(charactersToShow - 1) != ' ') {
            String startString = text.substring(0, charactersToShow);
            int start = startString.lastIndexOf(' ') + 1;

            String endString = text.substring(charactersToShow - 1);
            int end = endString.indexOf(' ');
            end = end == -1 ? endString.length() - 1 : end;

            lastWordLength = end - start + charactersToShow - 1;
        } else {
            lastWordLength = -1;
        }

        String drawText = text.substring(0, charactersToShow);
        return new TextWrapper(g, drawText, lastWordLength, startX, startY, textWidth).fits(bottomY);
    }
}
