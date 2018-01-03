package gui.view.mainmenu;

import draw.panel.DrawPanel;
import util.FileIO;
import util.Folder;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public enum Theme {
    BASIC(new Color(255, 210, 86), (g, bgTime, bgIndex) -> drawBasicTheme(g)),
    SCENIC(new Color(68, 123, 184), Theme::drawScenicTheme);

    private static final BufferedImage DFS_TOWN_BG = FileIO.readImage(Folder.IMAGES + "DFSTownSampleBackground.png");

    private static final int[] bgx = new int[] { -300, -400, -800, -800, -200 };
    private static final int[] bgy = new int[] { -300, -130, -130, -500, -500 };

    private final Color buttonColor;
    private final ThemeDrawer draw;

    Theme(Color buttonColor, ThemeDrawer draw) {
        this.buttonColor = buttonColor;
        this.draw = draw;
    }

    @FunctionalInterface
    private interface ThemeDrawer {
        void draw(Graphics g, int bgTime, int bgIndex);
    }

    public Color getButtonColor() {
        return this.buttonColor;
    }

    public void draw(Graphics g, int bgTime, int bgIndex) {
        this.draw.draw(g, bgTime, bgIndex);
    }

    private static void drawBasicTheme(Graphics g) {
        DrawPanel.fullGamePanel()
                .withTransparentBackground(new Color(68, 123, 184))
                .withTransparentCount(2)
                .withBorderPercentage(3)
                .drawBackground(g);
    }

    private static void drawScenicTheme(Graphics g, int bgTime, int bgIndex) {
        float locRatio = 1.0f - (float) bgTime / (float) MainMenuView.bgt[(bgIndex + 1)%MainMenuView.bgt.length];
        int xLoc = (int) (bgx[bgIndex]*locRatio + (1.0f - locRatio)*bgx[(bgIndex + 1)%MainMenuView.bgt.length]);
        int yLoc = (int) (bgy[bgIndex]*locRatio + (1.0f - locRatio)*bgy[(bgIndex + 1)%MainMenuView.bgt.length]);

        g.drawImage(DFS_TOWN_BG, xLoc, yLoc, null);

        DrawPanel.fullGamePanel()
                .withBackgroundColor(null)
                .withBorderColor(new Color(255, 255, 255, 200))
                .withBorderPercentage(3)
                .drawBackground(g);
    }
}
