package draw.panel;

import battle.attack.Attack;
import draw.panel.WrapPanel.WrapMetrics;
import util.FontMetrics;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class VerticalMovePanel extends DrawPanel {
    private final int nameFontSize;
    private final int basicFontSize;
    private final int descFontSize;

    // If the description exceeds the space in the panel, it will adjust the font size (should be smaller to make sense)
    private int minDescFontSize;

    public VerticalMovePanel(int x, int y, int width, int height, int nameFontSize, int basicFontSize, int descFontSize) {
        super(x, y, width, height);

        this.nameFontSize = nameFontSize;
        this.basicFontSize = basicFontSize;
        this.descFontSize = descFontSize;

        this.withMinDescFontSize(descFontSize);
        this.withBlackOutline();
        this.withTransparentBackground();
    }

    @Override
    public VerticalMovePanel withFullTransparency() {
        return (VerticalMovePanel)super.withFullTransparency();
    }

    public VerticalMovePanel withMinDescFontSize(int fontSize) {
        this.minDescFontSize = fontSize;
        return this;
    }

    public WrapMetrics draw(Graphics g, Attack attack) {
        // Draw type-colored background
        this.withBackgroundColor(attack.getActualType().getColor())
            .drawBackground(g);

        FontMetrics.setBlackFont(g, nameFontSize);
        int fullSpacing = this.getTextSpace(g);
        int borderSize = this.getBorderSize();

        int x = this.x + fullSpacing;
        int rightX = this.rightX() - fullSpacing;
        int textY = this.y + fullSpacing + FontMetrics.getTextHeight(g);

        // Draw the name in the top left
        g.drawString(attack.getName(), x, textY);

        // Draw the power underneath the name and the type on the right
        textY += FontMetrics.getDistanceBetweenRows(g, basicFontSize);
        g.drawString("Pow: " + attack.getPowerString(), x, textY);
        drawImage(g, attack.getActualType().getImage(), rightX, textY);

        // Draw the accuracy under the power and the category under the type
        textY += FontMetrics.getDistanceBetweenRows(g);
        g.drawString("Acc: " + attack.getAccuracyString(), x, textY);
        drawImage(g, attack.getCategory().getImage(), rightX, textY);

        // Draw the description underneath everything else as wrapped text
        // Will always be right-aligned with the name and power
        int startY = textY;
        WrapPanel descriptionPanel = new WrapPanel(
                this.x + borderSize,
                startY,
                this.width - 2*borderSize,
                this.bottomY() - startY - borderSize,
                descFontSize
        )
                .withBorderPercentage(0)
                .withMinimumSpacing(2)
                .withStartX(x)
                .withMinFontSize(this.minDescFontSize, true);
        return descriptionPanel.drawMessage(g, attack.getDescription());
    }

    private void drawImage(Graphics g, BufferedImage image, int rightX, int textY) {
        int imageY = textY - image.getHeight();
        int imageX = rightX - image.getWidth();
        g.drawImage(image, imageX, imageY, null);
    }
}
