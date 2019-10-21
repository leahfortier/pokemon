package draw.panel;

import draw.TextUtils;
import main.Global;
import map.Direction;
import message.MessageUpdate;
import pokemon.stat.Stat;
import util.FontMetrics;

import java.awt.Graphics;

public class StatGainPanel extends DrawPanel {
    private static final int panelHeight = BasicPanels.getMessagePanelHeight();

    // Has a fixed size and spacing above the message panel on the left side (same height as message panel)
    public StatGainPanel() {
        super(0, BasicPanels.getMessagePanelY() - panelHeight, 273, panelHeight);
        this.withMissingBlackOutline(Direction.DOWN);
    }

    public void drawStatGain(Graphics g, MessageUpdate message) {
        if (!message.gainUpdate()) {
            Global.error("Update type should be check before calling drawStatGain");
        }

        this.drawBackground(g);

        int[] statGains = message.getGain();
        int[] newStats = message.getNewStats();

        // Calculate equal spacing between each row (always one row for each stat)
        FontMetrics.setBlackFont(g, 16);
        int borderSize = this.getBorderSize();
        int textHeight = FontMetrics.getTextHeight(g);
        int totalSpacing = this.height - 2*borderSize - Stat.NUM_STATS*textHeight;
        int spacing = (int)Math.ceil((double)totalSpacing/(Stat.NUM_STATS + 1));

        int nameX = x + borderSize + spacing;
        int statRightX = this.rightX() - borderSize - spacing;
        int gainRightX = statRightX - FontMetrics.getTextWidth(g)*4;

        int betweenRows = textHeight + spacing;
        int textY = y + borderSize + betweenRows;

        for (int i = 0; i < Stat.NUM_STATS; i++) {
            g.drawString(Stat.getStat(i, false).getName(), nameX, textY);

            TextUtils.drawRightAlignedString(g, (statGains[i] < 0 ? "" : "+") + statGains[i], gainRightX, textY);
            TextUtils.drawRightAlignedString(g, newStats[i] + "", statRightX, textY);

            textY += betweenRows;
        }
    }
}
