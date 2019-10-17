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
    // If the same number of rows is desired, that should be specified here
    private int minDescFontSize;

    public MovePanel(DrawPanel drawPanel, int nameFontSize, int basicFontSize, int descFontSize) {
        this(drawPanel.x, drawPanel.y, drawPanel.width, drawPanel.height, nameFontSize, basicFontSize, descFontSize);
    }

    public MovePanel(int x, int y, int width, int height, int nameFontSize, int basicFontSize, int descFontSize) {
        super(x, y, width, height);

        this.nameFontSize = nameFontSize;
        this.basicFontSize = basicFontSize;
        this.descFontSize = descFontSize;

        this.withMinDescFontSize(descFontSize);
        this.withBlackOutline();
    }

    @Override
    public MovePanel withMissingBlackOutline(Direction missingBlackOutline) {
        return super.withMissingBlackOutline(missingBlackOutline).asMovePanel();
    }

    @Override
    public MovePanel withFullTransparency() {
        return super.withFullTransparency().asMovePanel();
    }

    @Override
    public MovePanel withTransparentCount(int transparentCount) {
        return super.withTransparentCount(transparentCount).asMovePanel();
    }

    @Override
    public MovePanel withBorderPercentage(int borderPercentage) {
        return super.withBorderPercentage(borderPercentage).asMovePanel();
    }

    public MovePanel withMinDescFontSize(int fontSize) {
        this.minDescFontSize = fontSize;
        return this;
    }

    public WrapMetrics draw(Graphics g, Attack attack) {
        // Draw type-colored background
        this.withTransparentBackground(attack.getActualType().getColor())
            .drawBackground(g);

        FontMetrics.setFont(g, nameFontSize);
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
