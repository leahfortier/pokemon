package draw.panel;

import draw.button.Button;
import main.Global;
import util.FontMetrics;
import util.Point;

import java.awt.Graphics;

public class BasicPanels {
    private BasicPanels() { Global.error("BasicPanels cannot be instantiated"); }

    private static final DrawPanel fullMessagePanel = new DrawPanel(0, 440, Global.GAME_SIZE.width, 161).withBlackOutline().withTextAnimation();
    private static final DrawPanel fullCanvasPanel = DrawPanel.fullGamePanel().withBorderPercentage(2).withBlackOutline();

    public static final Point canvasMessageCenter = new Point(
            Global.GAME_SIZE.width/2,
            BasicPanels.getMessagePanelY()/2
    );

    public static int drawFullMessagePanel(Graphics g, String text) {
        fullMessagePanel.drawBackground(g);
        return fullMessagePanel.drawMessage(g, 30, text);
    }

    public static Button[] getFullMessagePanelButtons(int buttonWidth, int buttonHeight, int numRows, int numCols) {
        return fullMessagePanel.getButtons(buttonWidth, buttonHeight, numRows, numCols);
    }

    public static void drawCanvasPanel(Graphics g) {
        fullCanvasPanel.drawBackground(g);
    }

    public static int getMessagePanelY() {
        return fullMessagePanel.y;
    }

    public static DrawPanel getLabelPanel(int x, int y, int fontSize, int spacing, String label) {
        return new DrawPanel(
                x,
                y,
                FontMetrics.getTextWidth(fontSize, label) + 2*spacing,
                FontMetrics.getTextHeight(fontSize) + 2*spacing
        ).withFullTransparency()
         .withBlackOutline();
    }

    public static DrawPanel drawLabelPanel(Graphics g, int x, int y, int fontSize, int spacing, String label) {
        DrawPanel drawPanel = getLabelPanel(x, y, fontSize, spacing, label);
        drawPanel.drawBackground(g);
        drawPanel.label(g, fontSize, label);

        return drawPanel;
    }

    public static boolean isAnimatingMessage() {
        return fullMessagePanel.isAnimatingMessage();
    }
}
