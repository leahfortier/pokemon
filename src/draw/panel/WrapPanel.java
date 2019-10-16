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

        int startX = spacing.startX;
        int startY = spacing.startY;
        int textWidth = spacing.textWidth;
        int bottomY = this.bottomY() - this.getBorderSize();


        final TextWrapper textWrapper;
        if (this.animateMessage) {
            // Animated messages
            textWrapper = this.drawAnimatedText(g, text, startX, startY, textWidth);
        } else {
            // Flat text
            textWrapper = new TextWrapper(text, startX, startY, textWidth).draw(g);
        }

        // Returns metrics (whether it fits, which font size was used, etc)
        return new WrapMetrics(textWrapper, spacing, bottomY);
    }

    private TextWrapper drawAnimatedText(Graphics g, String text, int startX, int startY, int textWidth) {
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
        return new TextWrapper(drawText, startX, startY, textWidth).draw(g, lastWordLength);
    }

    private Spacing getSpacing(Graphics g, String text) {
        int fontSize = this.fontSize;

        // Create the spacing details for the default font size
        Spacing spacing = new Spacing(g, fontSize, startX, minimumSpacing);

        int defaultVerticalSpacing = Math.max(this.minimumSpacing, spacing.textSpace/2);
        int minimumSpacing = this.backupSameSpacing ? defaultVerticalSpacing : this.minimumSpacing;
        int startX = this.backupSameSpacing ? spacing.startX : this.startX;

        while (true) {
            // Use if the text fits in the current spacing or if this is the minimum font size
            if (spacing.fits(g, text) || fontSize == this.minFontSize) {
                return spacing;
            }

            // Otherwise, decrease the font size and create a new spacing layout
            spacing = new Spacing(g, --fontSize, startX, minimumSpacing);
        }
    }

    private class Spacing {
        private final int fontSize;

        private final int maxRows;
        private final int textSpace;

        private final int startX;
        private final int startY;
        private final int textWidth;

        public Spacing(Graphics g, int fontSize, int startX, int minimumSpacing) {
            this.fontSize = fontSize;
            FontMetrics.setFont(g, fontSize);

            // Include the outline size if there is no inset border
            // I realize not every panel is bordered, but it's probably fine for the ones that don't and is sometimes
            // necessary for that ones and having logic everywhere for different outline edges is overly complicated
            int borderSize = Math.max(getBorderSize(), DrawUtils.OUTLINE_SIZE) + minimumSpacing;

            // Determine the maximum number of rows of text that could properly fit
            int totalPotentialHeight = height - 2*borderSize;
            int distanceBetweenRows = FontMetrics.getDistanceBetweenRows(g);
            this.maxRows = totalPotentialHeight/distanceBetweenRows;

            int textHeight = FontMetrics.getTextHeight(g);
            int totalTextHeight = textHeight + distanceBetweenRows*(maxRows - 1);

            // Create spacing so that there is equal spacing on all sides when at the maximum number of rows
            this.textSpace = (totalPotentialHeight - totalTextHeight)/2;
            int fullSpace = textSpace + borderSize;

            // If a startX was specified, use that instead of the spacing
            this.startX = startX == -1 ? x + fullSpace : startX;
            this.startY = y + fullSpace + textHeight;
            this.textWidth = width - 2*(this.startX - x);
        }

        public boolean fits(Graphics g, String text) {
            TextWrapper wrapper = new TextWrapper(text, startX, startY, textWidth);
            return wrapper.numRows(g) <= this.maxRows;
        }
    }

    public static class WrapMetrics {
        private final int fontSize;
        private final boolean fits;

        private WrapMetrics(TextWrapper textWrapper, Spacing spacing, int bottomY) {
            this.fontSize = spacing.fontSize;
            this.fits = textWrapper.fits(bottomY);
        }

        public int getFontSize() {
            return this.fontSize;
        }

        public boolean fits() {
            return this.fits;
        }
    }
}
