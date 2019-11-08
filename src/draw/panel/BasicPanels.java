package draw.panel;

import draw.layout.ButtonLayout;
import main.Global;

import java.awt.Graphics;

public class BasicPanels {
    private static final WrapPanel fullMessagePanel = new WrapPanel(0, 440, Global.GAME_SIZE.width, 161, 30)
            .withBlackOutline()
            .withTextAnimation();

    private static final DrawPanel fullCanvasPanel = newFullGamePanel()
            .withBorderPercentage(2)
            .withBlackOutline();

    private BasicPanels() {
        Global.error(this.getClass().getSimpleName() + " class cannot be instantiated.");
    }

    public static DrawPanel newFullGamePanel() {
        return new DrawPanel(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
    }

    // Canvas panel truncated at the message
    public static DrawPanel newMessagelessCanvasPanel() {
        return new DrawPanel(
                fullCanvasPanel.x,
                fullCanvasPanel.y,
                fullCanvasPanel.width,
                fullMessagePanel.y - fullCanvasPanel.y
        );
    }

    public static void drawFullMessagePanel(Graphics g, String text) {
        fullMessagePanel.drawBackground(g);
        fullMessagePanel.drawMessage(g, text);
    }

    public static ButtonLayout getFullMessagePanelLayout(int numRows, int numCols, int spacing) {
        return new ButtonLayout(fullMessagePanel, numRows, numCols, spacing);
    }

    public static void drawCanvasPanel(Graphics g) {
        fullCanvasPanel.drawBackground(g);
    }

    public static int getMessagePanelY() {
        return fullMessagePanel.y;
    }

    public static int getMessagePanelHeight() {
        return fullMessagePanel.height;
    }

    public static boolean isAnimatingMessage() {
        return fullMessagePanel.isAnimatingMessage();
    }
}
