package draw.panel;

import battle.attack.Move;
import draw.TextUtils;
import draw.button.ButtonPanel;
import util.FontMetrics;

import java.awt.Graphics;

public class MoveButtonPanel {
    private final ButtonPanel parent;
    private final int nameFontSize;
    private final int ppFontSize;

    private final int spacing;

    private Move move;

    public MoveButtonPanel(ButtonPanel parent, int nameFontSize, int ppFontSize) {
        this.parent = parent;
        this.nameFontSize = nameFontSize;
        this.ppFontSize = ppFontSize;

        this.spacing = FontMetrics.getTextWidth(nameFontSize)/2;
    }

    public void setMove(Move move) {
        this.move = move;
        parent.withBackgroundColor(move.getAttack().getActualType().getColor());
    }

    // Only draws the move foreground text and the background colors etc is handled separately
    public void drawMove(Graphics g) {
        int borderSize = parent.getBorderSize();
        int fullSpacing = spacing + borderSize;

        // Attack name as left label on the top
        FontMetrics.setBlackFont(g, nameFontSize);
        String attackName = move.getAttack().getName();
        g.drawString(attackName, parent.x + fullSpacing, parent.y + fullSpacing + FontMetrics.getTextHeight(g));

        // PP amount as right label on the bottom
        FontMetrics.setBlackFont(g, ppFontSize);
        String ppString = "PP: " + move.getPPString();
        TextUtils.drawRightAlignedString(g, ppString, parent.rightX() - fullSpacing, parent.bottomY() - fullSpacing);
    }
}
