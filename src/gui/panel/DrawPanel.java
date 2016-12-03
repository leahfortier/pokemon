package gui.panel;

import java.awt.Color;
import java.awt.Graphics;

public class DrawPanel {
    protected final int x;
    protected final int y;
    protected final int width;
    protected final int height;

    private int borderPercentage;

    private Color bgColor;
    private Color borderColor;

    public DrawPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.borderPercentage = 10;

        this.bgColor = Color.WHITE;
        this.borderColor = Color.LIGHT_GRAY;
    }

    public DrawPanel withBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public DrawPanel withDarkBorder() {
        return this.withBorderColor(this.bgColor.darker());
    }

    public DrawPanel withBackgroundColor(Color bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public DrawPanel withBorderPercentage(int borderPercentage) {
        this.borderPercentage = borderPercentage;
        return this;
    }

    protected int getBorderSize() {
        return (int)(borderPercentage/100.0*Math.min(width, height));
    }

    public void drawBackground(Graphics g) {
        g.setColor(bgColor);
        g.fillRect(x, y, width, height);

        int borderSize = this.getBorderSize();

        g.setColor(borderColor);
        g.fillRect(x, y, width, borderSize);
        g.fillRect(x, y, borderSize, height);
        g.fillRect(x, y + height - borderSize, width, borderSize);
        g.fillRect(x + width - borderSize, y, borderSize, height);
    }
}
