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

    private int minimumSpacing;
    private int startX;

    private int minFontSize;
    private boolean backupSameSpacing;

    private boolean animateMessage;

    private int messageTimeElapsed;
    private String drawingText;
    private boolean finishedAnimating;

    public WrapPanel(DrawPanel panel, int fontSize) {
        this(panel.x, panel.y, panel.width, panel.height, fontSize);
    }

    public WrapPanel(int x, int y, int width, int height, int fontSize) {
        super(x, y, width, height);

        this.fontSize = fontSize;

        this.messageTimeElapsed = 0;
        this.finishedAnimating = true;

        this.withStartX(-1);
        this.withMinFontSize(fontSize, false);
        this.withMinimumSpacing(1);
    }

    @Override
    public WrapPanel withBlackOutline() {
        return (WrapPanel)super.withBlackOutline();
    }

    @Override
    public WrapPanel withBackgroundColor(Color backgroundColor) {
        return (WrapPanel)super.withBackgroundColor(backgroundColor);
    }

    @Override
    public WrapPanel withBorderColor(Color borderColor) {
        return (WrapPanel)super.withBorderColor(borderColor);
    }

    @Override
    public WrapPanel withBorderPercentage(int borderPercentage) {
        return (WrapPanel)super.withBorderPercentage(borderPercentage);
    }

    @Override
    public WrapPanel withBorderSize(int borderSize) {
        return (WrapPanel)super.withBorderSize(borderSize);
    }

    @Override
    public WrapPanel withTransparentBackground() {
        return (WrapPanel)super.withTransparentBackground();
    }

    @Override
    public WrapPanel withFullTransparency() {
        return (WrapPanel)super.withFullTransparency();
    }

    public WrapPanel withMinFontSize(int minFontSize, boolean sameSpacing) {
        this.minFontSize = minFontSize;
        this.backupSameSpacing = sameSpacing;
        return this;
    }

    public WrapPanel withStartX(int startX) {
        this.startX = startX;
        return this;
    }

    public WrapPanel withMinimumSpacing(int minimumSpacing) {
        this.minimumSpacing = minimumSpacing;
        return this;
    }

    public WrapPanel withTextAnimation() {
        this.animateMessage = true;
        return this;
    }

    public boolean isAnimatingMessage() {
        return !finishedAnimating && animateMessage;
    }

    // Draws the text, wrapping to the next line if necessary
    // Returns some helpful metrics about the wrap (if it fits in the panel, which font size used, etc.)
    public WrapMetrics drawMessage(Graphics g, String text) {
        g.setColor(Color.BLACK);

        // Get spacing so that there is an equal amount of space when using all of the potential wrap lines
        // Font size will be set inside (may not be the original font size)
        Spacing spacing = this.getSpacing(g, text);

        if (this.animateMessage) {
            // Animated messages
            this.drawAnimatedText(g, spacing);
        } else {
            // Flat text
            spacing.wrapper.draw(g);
        }

        // Returns metrics (whether it fits, which font size was used, etc)
        return new WrapMetrics(spacing);
    }

    private void drawAnimatedText(Graphics g, Spacing spacing) {
        String text = spacing.text;
        int startX = spacing.startX;
        int startY = spacing.startY;
        int textWidth = spacing.textWidth;

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
        new TextWrapper(drawText, startX, startY, textWidth).draw(g, lastWordLength);
    }

    // Returns the best spacing for the input text
    // Does not handle any drawing logic, just gets the proper coordinates and such set up
    // Mostly is selecting which font size is most appropriate
    private Spacing getSpacing(Graphics g, String text) {
        int fontSize = this.fontSize;

        // Create the spacing details for the default font size
        Spacing spacing = new Spacing(g, text, fontSize, startX, minimumSpacing);

        int defaultVerticalSpacing = Math.max(this.minimumSpacing, spacing.textSpace/2);
        int minimumSpacing = this.backupSameSpacing ? defaultVerticalSpacing : this.minimumSpacing;
        int startX = this.backupSameSpacing ? spacing.startX : this.startX;

        while (true) {
            // Use if the text fits in the current spacing or if this is the minimum font size
            if (spacing.fits || fontSize == this.minFontSize) {
                return spacing;
            }

            // Otherwise, decrease the font size and create a new spacing layout
            spacing = new Spacing(g, text, --fontSize, startX, minimumSpacing);
        }
    }

    private class Spacing {
        private final String text;
        private final int fontSize;

        private final int textSpace;

        private final int startX;
        private final int startY;
        private final int textWidth;

        private final TextWrapper wrapper;
        private final boolean fits;

        public Spacing(Graphics g, String text, int fontSize, int startX, int minimumSpacing) {
            this.text = text;
            this.fontSize = fontSize;
            FontMetrics.setFont(g, fontSize);

            // Include the outline size if there is no inset border
            // I realize not every panel is bordered, but it's probably fine for the ones that don't and is sometimes
            // necessary for that ones and having logic everywhere for different outline edges is overly complicated
            int borderSize = Math.max(getBorderSize(), DrawUtils.OUTLINE_SIZE) + minimumSpacing;

            // Determine the maximum number of rows of text that could properly fit
            int totalPotentialHeight = height - 2*borderSize;
            int distanceBetweenRows = FontMetrics.getDistanceBetweenRows(g);
            int maxRows = totalPotentialHeight/distanceBetweenRows;

            int textHeight = FontMetrics.getTextHeight(g);
            int totalTextHeight = textHeight + distanceBetweenRows*(maxRows - 1);

            // Create spacing so that there is equal spacing on all sides when at the maximum number of rows
            this.textSpace = (totalPotentialHeight - totalTextHeight)/2;
            int fullSpace = textSpace + borderSize;

            // If a startX was specified, use that instead of the spacing
            this.startX = startX == -1 ? x + fullSpace : startX;
            this.startY = y + fullSpace + textHeight;
            this.textWidth = width - 2*(this.startX - x);

            this.wrapper = new TextWrapper(text, this.startX, startY, textWidth);
            this.fits = wrapper.numRows(g) <= maxRows;
        }
    }

    public static class WrapMetrics {
        private final int fontSize;
        private final boolean fits;

        private WrapMetrics(Spacing spacing) {
            this.fontSize = spacing.fontSize;
            this.fits = spacing.fits;
        }

        public int getFontSize() {
            return this.fontSize;
        }

        public boolean fits() {
            return this.fits;
        }
    }
}
