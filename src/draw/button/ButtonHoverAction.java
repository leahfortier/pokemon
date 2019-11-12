package draw.button;

import draw.PulseColor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

public enum ButtonHoverAction {
    BOX(new HoverActionDrawer() {
        private final Stroke lineStroke = new BasicStroke(5f);
        private PulseColor pulseColor = new PulseColor(Color.BLACK);

        @Override
        public void draw(Graphics g, Button button) {
            pulseColor.setColor(g);

            Graphics2D g2d = (Graphics2D)g;
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(lineStroke);
            g.drawRect(button.x - 2, button.y - 2, button.width + 3, button.height + 4);
            g2d.setStroke(oldStroke);
        }
    }),
    ARROW(new HoverActionDrawer() {
        private final int[] tx = { 0, 11, 0 };
        private final int[] ty = { 0, 12, 23 };
        private PulseColor pulseColor = new PulseColor(Color.BLACK);

        @Override
        public void draw(Graphics g, Button button) {
            pulseColor.setColor(g);

            int x = button.x - 10;
            int y = button.y + button.height/2 - 12;

            g.translate(x, y);
            g.fillPolygon(tx, ty, 3);
            g.translate(-x, -y);
        }
    });

    private final HoverActionDrawer hoverActionDrawer;

    ButtonHoverAction(HoverActionDrawer hoverActionDrawer) {
        this.hoverActionDrawer = hoverActionDrawer;
    }

    void draw(Graphics g, Button button) {
        hoverActionDrawer.draw(g, button);
    }

    private interface HoverActionDrawer {
        void draw(Graphics g, Button button);
    }
}
