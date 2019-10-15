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

    private int backupFontSize;
    private boolean backupSameMaxRows;

    private boolean animateMessage;

    private int messageTimeElapsed;
    private String drawingText;
    private boolean finishedAnimating;

    public WrapPanel(int x, int y, int width, int height, int fontSize) {
        super(x, y, width, height);

        this.fontSize = fontSize;

        this.messageTimeElapsed = 0;
        this.finishedAnimating = true;

        this.withBackupFontSize(fontSize, false);
        this.withMinimumSpacing(2);
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

    public WrapPanel withBackupFontSize(int fontSize, boolean sameMaxRows) {
        this.backupFontSize = fontSize;
        this.backupSameMaxRows = sameMaxRows;
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

    // Draws the text, wrapping to the next line if necessary and returns whether or not the text
    // fits entirely inside the panel
    public boolean drawMessage(Graphics g, String text) {
        FontMetrics.setFont(g, fontSize);
        g.setColor(Color.BLACK);

        // Get spacing so that there is an equal amount of space when using all of the potential wrap lines
        Spacing spacing = getSpacing(g, text);

        int startX = spacing.startX;
        int startY = spacing.startY;
        int textWidth = spacing.textWidth;
        int bottomY = this.bottomY() - this.getBorderSize();

        if (!this.animateMessage) {
            return new TextWrapper(text, startX, startY, textWidth).draw(g).fits(bottomY);
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
        return new TextWrapper(drawText, startX, startY, textWidth).draw(g, lastWordLength).fits(bottomY);
    }

    private Spacing getSpacing(Graphics g, String text) {
        // Create the spacing details
        Spacing spacing = new Spacing(g);

        // If the text fits in the default spacing, then use that
        if (spacing.fits(g, text)) {
            return spacing;
        }

        // Otherwise, use the backup font size and create a new spacing layout
        FontMetrics.setFont(g, backupFontSize);

        // Create a new layout with the same number of maxRows, but only use the vertical spacing
        if (backupSameMaxRows) {
            Spacing backupSpacing = new Spacing(g, spacing.maxRows);
            return new Spacing(spacing, backupSpacing.startY);
        } else {
            // Just create a full new spacing layout
            return new Spacing(g);
        }
    }

    private class Spacing {
        private final int maxRows;

        private final int startX;
        private final int startY;
        private final int textWidth;

        public Spacing(Spacing other, int startY) {
            this.maxRows = other.maxRows;
            this.startX = other.startX;
            this.startY = startY;
            this.textWidth = other.textWidth;
        }

        public Spacing(Graphics g) {
            this(g, -1);
        }

        public Spacing(Graphics g, int maxRows) {
            // Include the outline size if there is no inset border
            // I realize not every panel is bordered, but it's probably fine for the ones that don't and is sometimes
            // necessary for that ones and having logic everywhere for different outline edges is overly complicated
            int borderSize = Math.max(getBorderSize(), DrawUtils.OUTLINE_SIZE) + minimumSpacing;

            // Determine the maximum number of rows of text that could properly fit
            int totalPotentialHeight = height - 2*borderSize;
            int distanceBetweenRows = FontMetrics.getDistanceBetweenRows(g);
            this.maxRows = maxRows == -1 ? totalPotentialHeight/distanceBetweenRows : maxRows;

            int textHeight = FontMetrics.getTextHeight(g);
            int totalTextHeight = textHeight + distanceBetweenRows*(this.maxRows - 1);

            // Create spacing so that there is equal spacing on all sides when at the maximum number of rows
            int textSpace = (totalPotentialHeight - totalTextHeight)/2;
            int fullSpace = textSpace + borderSize;

            this.startX = x + fullSpace;
            this.startY = y + fullSpace + FontMetrics.getTextHeight(g);
            this.textWidth = width - 2*fullSpace;
        }

        public boolean fits(Graphics g, String text) {
            TextWrapper wrapper = new TextWrapper(text, startX, startY, textWidth);
            return wrapper.numRows(g) <= this.maxRows;
        }
    }
}
