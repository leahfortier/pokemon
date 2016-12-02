package gui.view.mainmenu;

import gui.TileSet;
import util.DrawUtils;

import java.awt.Color;
import java.awt.Graphics;

public enum Theme {
    BASIC(new Color(255, 210, 86), (g, tiles, bgTime, bgIndex) -> drawBasicTheme(g, tiles)),
    SCENIC(new Color(68, 123, 184), Theme::drawScenicTheme);

    private static final int[] bgx = new int[] { -300, -400, -800, -800, -200 };
    private static final int[] bgy = new int[] { -300, -130, -130, -500, -500 };

    private final Color themeColor;
    private final ThemeDrawer draw;

    Theme(Color themeColor, ThemeDrawer draw) {
        this.themeColor = themeColor;
        this.draw = draw;
    }

    private interface ThemeDrawer {
        void draw(Graphics g, TileSet tiles, int bgTime, int bgIndex);
    }

    public Color getThemeColor() {
        return this.themeColor;
    }

    public void draw(Graphics g, TileSet tiles, int bgTime, int bgIndex) {
        this.draw.draw(g, tiles, bgTime, bgIndex);
    }

    private static void drawBasicTheme(Graphics g, TileSet tiles) {
        DrawUtils.fillCanvas(g, new Color(68, 123, 184));
        g.drawImage(tiles.getTile(0x01), 0, 0, null);
    }

    private static void drawScenicTheme(Graphics g, TileSet tiles, int bgTime, int bgIndex) {
        float locRatio = 1.0f - (float) bgTime / (float) MainMenuView.bgt[(bgIndex + 1) % MainMenuView.bgt.length];
        int xLoc = (int) (Theme.bgx[bgIndex]*locRatio + (1.0f - locRatio)*Theme.bgx[(bgIndex + 1)%MainMenuView.bgt.length]);
        int yLoc = (int) (Theme.bgy[bgIndex]*locRatio + (1.0f - locRatio)*Theme.bgy[(bgIndex + 1)%MainMenuView.bgt.length]);

        g.drawImage(tiles.getTile(0x06), xLoc, yLoc, null);
        g.drawImage(tiles.getTile(0x02), 0, 0, null);
    }
}
