package draw.panel;

import battle.attack.Attack;
import draw.TextUtils;
import draw.panel.WrapPanel.WrapMetrics;
import map.Direction;
import util.FontMetrics;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class MovePanel extends DrawPanel {
    private final int nameFontSize;
    private final int basicFontSize;
    private final int descFontSize;

    // If the description exceeds the space in the panel, it will adjust the font size (should be smaller to make sense)
    private int minDescFontSize;

    public MovePanel(Panel sizing, int nameFontSize, int basicFontSize, int descFontSize) {
        this(sizing.getX(), sizing.getY(), sizing.getWidth(), sizing.getHeight(), nameFontSize, basicFontSize, descFontSize);
    }

    public MovePanel(int x, int y, int width, int height, int nameFontSize, int basicFontSize, int descFontSize) {
        super(x, y, width, height);

        this.nameFontSize = nameFontSize;
        this.basicFontSize = basicFontSize;
        this.descFontSize = descFontSize;

        this.withMinDescFontSize(descFontSize);
        this.withBlackOutline();
        this.withTransparentBackground();
    }

    @Override
    public MovePanel withMissingBlackOutline(Direction missingBlackOutline) {
        return (MovePanel)super.withMissingBlackOutline(missingBlackOutline);
    }

    @Override
    public MovePanel withFullTransparency() {
        return (MovePanel)super.withFullTransparency();
    }

    @Override
    public MovePanel withTransparentCount(int transparentCount) {
        return (MovePanel)super.withTransparentCount(transparentCount);
    }

    @Override
    public MovePanel withBorderPercentage(int borderPercentage) {
        return (MovePanel)super.withBorderPercentage(borderPercentage);
    }

    public MovePanel withMinDescFontSize(int fontSize) {
        this.minDescFontSize = fontSize;
        return this;
    }

    // Draws both background and foreground
    public WrapMetrics draw(Graphics g, Attack attack) {
        // Draw type-colored background
        this.withBackgroundColor(attack.getActualType().getColor())
            .drawBackground(g);

        return drawMove(g, attack);
    }

    // Only draws foreground (text and images)
    public WrapMetrics drawMove(Graphics g, Attack attack) {
        FontMetrics.setBlackFont(g, nameFontSize);
        int textSpace = this.getTextSpace(g);
        int borderSize = this.getBorderSize();
        int betweenSpace = textSpace - borderSize;

        int x = this.x + textSpace;
        int rightX = this.rightX() - textSpace;
        int textY = this.y + textSpace + FontMetrics.getTextHeight(g);

        // Draw the name in the top left
        g.drawString(attack.getName(), x, textY);

        // Draw the type image in the top right
        BufferedImage typeImage = attack.getActualType().getImage();
        int imageY = textY - typeImage.getHeight();
        int imageX = rightX - typeImage.getWidth();
        g.drawImage(typeImage, imageX, imageY, null);

        // Draw the category image to the left of the type image
        BufferedImage categoryImage = attack.getCategory().getImage();
        imageX -= categoryImage.getWidth() + betweenSpace;
        g.drawImage(categoryImage, imageX, imageY, null);

        // Draw the power underneath the name and the accuracy underneath the images
        int previousDistanceBetweenRows = FontMetrics.getDistanceBetweenRows(g);
        FontMetrics.setFont(g, basicFontSize);
        textY += (previousDistanceBetweenRows + FontMetrics.getDistanceBetweenRows(g))/2;
        g.drawString("Power: " + attack.getPowerString(), x, textY);
        TextUtils.drawRightAlignedString(g, "Acc: " + attack.getAccuracyString(), rightX, textY);

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
}
