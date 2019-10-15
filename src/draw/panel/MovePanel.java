package draw.panel;

import battle.attack.Attack;
import draw.TextUtils;
import util.FontMetrics;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class MovePanel extends DrawPanel {
    private int nameFontSize;
    private int basicFontSize;
    private int descFontSize;

    public MovePanel(DrawPanel drawPanel) {
        this(drawPanel.x, drawPanel.y, drawPanel.width, drawPanel.height);
    }

    public MovePanel(int x, int y, int width, int height) {
        super(x, y, width, height);

        this.withFontSizes(24, 18, 16);
        this.withBlackOutline();
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

    public MovePanel withFontSizes(int nameFontSize, int basicFontSize, int descFontSize) {
        this.nameFontSize = nameFontSize;
        this.basicFontSize = basicFontSize;
        this.descFontSize = descFontSize;
        return this;
    }

    public boolean draw(Graphics g, Attack move) {
        // Draw type-colored background
        this.withTransparentBackground(move.getActualType().getColor())
            .drawBackground(g);

        FontMetrics.setFont(g, nameFontSize);
        int textSpace = this.getTextSpace(g);
        int borderSize = this.getBorderSize();
        int betweenSpace = textSpace - borderSize;
        int x = this.x + textSpace;
        int rightX = this.rightX() - textSpace;
        int textY = this.y + textSpace + FontMetrics.getTextHeight(g);

        // Draw the name in the top left
        g.drawString(move.getName(), x, textY);

        // Draw the type image in the top right
        BufferedImage typeImage = move.getActualType().getImage();
        int imageY = textY - typeImage.getHeight();
        int imageX = rightX - typeImage.getWidth();
        g.drawImage(typeImage, imageX, imageY, null);

        // Draw the category image to the left of the type image
        BufferedImage categoryImage = move.getCategory().getImage();
        imageX -= categoryImage.getWidth() + betweenSpace;
        g.drawImage(categoryImage, imageX, imageY, null);

        // Draw the power underneath the name and the accuracy underneath the images
        FontMetrics.setFont(g, basicFontSize);
        textY += FontMetrics.getDistanceBetweenRows(g);
        g.drawString("Power: " + move.getPowerString(), x, textY);
        TextUtils.drawRightAlignedString(g, "Acc: " + move.getAccuracyString(), rightX, textY);

        // Draw the description underneath everything else as wrapped text
        int startY = textY + 2;
        WrapPanel descriptionPanel = new WrapPanel(
                this.x + borderSize,
                startY,
                this.width - 2*borderSize,
                this.bottomY() - startY - borderSize,
                descFontSize
        ).withBorderPercentage(0);
        return descriptionPanel.drawMessage(g, move.getDescription());
    }
}
