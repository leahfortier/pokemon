package draw.panel;

import draw.Alignment;
import draw.TextUtils;
import draw.layout.DrawLayout;
import pokemon.active.PartyPokemon;
import pokemon.active.StatValues;
import pokemon.stat.Stat;
import util.FontMetrics;

import java.awt.Graphics;

public class StatPanel extends DrawPanel {
    private final int nameFontSize;
    private final int valueFontSize;

    private boolean includeCurrentHp;
    private int insetSpaces;

    // Spacing values
    private int borderSizeUsed;
    private DrawPanel[] panels;
    private int nameX;
    private int evRightX;
    private int ivRightX;
    private int statRightX;

    public StatPanel(int x, int y, int width, int height, int nameFontSize, int valueFontSize) {
        super(x, y, width, height);

        this.nameFontSize = nameFontSize;
        this.valueFontSize = valueFontSize;

        this.includeCurrentHp = false;
        this.insetSpaces = 2;

        this.borderSizeUsed = -1;
        this.setPanels();
    }

    @Override
    public StatPanel withFullTransparency() {
        return (StatPanel)super.withFullTransparency();
    }

    @Override
    public StatPanel withBlackOutline() {
        return (StatPanel)super.withBlackOutline();
    }

    public StatPanel includeCurrentHp() {
        includeCurrentHp = true;
        return this;
    }

    public StatPanel withInsetSpaces(int insetSpaces) {
        this.insetSpaces = insetSpaces;
        return this;
    }

    private void setPanels() {
        int borderSize = this.getBorderSize();
        if (borderSize == this.borderSizeUsed) {
            return;
        }

        this.borderSizeUsed = borderSize;

        int textHeight = FontMetrics.getTextHeight(nameFontSize);
        panels = new DrawLayout(this, Stat.NUM_STATS + 1, 1, this.width, textHeight).getPanels();

        int textWidth = FontMetrics.getTextWidth(nameFontSize);
        int inset = borderSize + insetSpaces*textWidth;
        int statSpace = 6*textWidth;
        nameX = this.x + inset;
        evRightX = this.rightX() - inset;
        ivRightX = evRightX - statSpace;
        statRightX = ivRightX - statSpace;
    }

    public void drawStats(Graphics g, PartyPokemon selected) {
        this.setPanels();

        FontMetrics.setBlackFont(g, nameFontSize);
        drawStats(g, panels[0], "Stat", "IV", "EV");
        for (int i = 0; i < Stat.NUM_STATS; i++) {
            DrawPanel panel = panels[i + 1];
            Stat stat = Stat.getStat(i, false);

            FontMetrics.setFont(g, nameFontSize, selected.getNature().getColor(stat));
            TextUtils.drawCenteredHeightString(g, stat.getName(), nameX, panel.centerY());

            FontMetrics.setBlackFont(g, valueFontSize);
            drawStats(g, panel, selected, stat);
        }
    }

    private void drawStats(Graphics g, DrawPanel panel, PartyPokemon selected, Stat s) {
        StatValues stats = selected.stats();

        String stat = stats.get(s) + "";
        String iv = stats.getIVs().get(s) + "";
        String ev = stats.getEVs().get(s) + "";

        // Include the full HP String if specified
        if (s == Stat.HP && this.includeCurrentHp) {
            stat = selected.getHpString();
        }

        this.drawStats(g, panel, stat, iv, ev);
    }

    private void drawStats(Graphics g, DrawPanel panel, String stat, String iv, String ev) {
        TextUtils.drawCenteredHeightString(g, stat, statRightX, panel.centerY(), Alignment.RIGHT);
        TextUtils.drawCenteredHeightString(g, iv, ivRightX, panel.centerY(), Alignment.RIGHT);
        TextUtils.drawCenteredHeightString(g, ev, evRightX, panel.centerY(), Alignment.RIGHT);
    }
}
