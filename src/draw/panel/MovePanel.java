package draw.panel;

import battle.attack.Attack;
import draw.TextUtils;
import util.FontMetrics;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class MovePanel extends DrawPanel {
    private int spacing;

    private int nameFontSize;
    private int basicFontSize;
    private int descFontSize;

    public MovePanel(DrawPanel drawPanel) {
        this(drawPanel.x, drawPanel.y, drawPanel.width, drawPanel.height);
    }

    public MovePanel(int x, int y, int width, int height) {
        super(x, y, width, height);

        this.withSpacing(20);
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

    // TODO: Should use border percentage instead of spacing
    public MovePanel withSpacing(int spacing) {
        this.spacing = spacing;
        return this;
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
        int textY = this.y + spacing + FontMetrics.getTextHeight(g);
        g.drawString(move.getName(), this.x + spacing, textY);

        BufferedImage typeImage = move.getActualType().getImage();
        int imageY = textY - typeImage.getHeight();
        int imageX = this.rightX() - spacing - typeImage.getWidth();
        g.drawImage(typeImage, imageX, imageY, null);

        BufferedImage categoryImage = move.getCategory().getImage();
        imageX -= categoryImage.getWidth() + spacing;
        g.drawImage(categoryImage, imageX, imageY, null);

        textY += FontMetrics.getDistanceBetweenRows(g);

        FontMetrics.setFont(g, basicFontSize);
        g.drawString("Power: " + move.getPowerString(), this.x + spacing, textY);
        TextUtils.drawRightAlignedString(g, "Acc: " + move.getAccuracyString(), this.rightX() - spacing, textY);

        textY += 2;
        int borderSize = this.getBorderSize();
        WrapPanel descriptionPanel = new WrapPanel(
                x + borderSize,
                textY + borderSize,
                this.width - 2*borderSize,
                this.bottomY() - textY - 2*borderSize,
                descFontSize
        ).withBorderPercentage(0);
        return descriptionPanel.drawMessage(g, move.getDescription());
    }
}
