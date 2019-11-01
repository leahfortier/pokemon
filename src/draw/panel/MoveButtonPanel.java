package draw.panel;

import battle.attack.Move;
import draw.TextUtils;
import util.FontMetrics;

import java.awt.Graphics;

public class MoveButtonPanel extends DrawPanel {
    private final int nameFontSize;
    private final int ppFontSize;

    private Move move;

    public MoveButtonPanel(Panel sizing, int nameFontSize, int ppFontSize) {
        super(sizing);

        this.nameFontSize = nameFontSize;
        this.ppFontSize = ppFontSize;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    // Only draws the move foreground text and the background colors etc is handled separately
    public void drawMove(Graphics g) {
        FontMetrics.setBlackFont(g, nameFontSize);
        int spacing = FontMetrics.getTextWidth(g)/2;
        int borderSize = this.getBorderSize();
        int fullSpacing = spacing + borderSize;

        // Attack name as left label on the top
        g.drawString(move.getAttack().getName(), x + fullSpacing, y + fullSpacing + FontMetrics.getTextHeight(g));

        // PP amount as right label on the bottom
        FontMetrics.setBlackFont(g, ppFontSize);
        String ppString = "PP: " + move.getPPString();
        TextUtils.drawRightAlignedString(g, ppString, rightX() - fullSpacing, bottomY() - fullSpacing);
    }
}
