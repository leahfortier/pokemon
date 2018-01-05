package draw.button;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

public enum ButtonHoverAction {
    BOX(new HoverActionDrawer() {
        private final Stroke lineStroke = new BasicStroke(5f);
        private int time = 0;
        
        public void draw(Graphics g, Button button) {
            time = (time + 1)%80;
            
            g.setColor(new Color(0, 0, 0, 55 + 150*(Math.abs(time - 40))/40));
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
        private int time = 0;
        
        public void draw(Graphics g, Button button) {
            time = (time + 1)%80;
            
            int x = button.x - 10;
            int y = button.y + button.height/2 - 12;
            
            g.translate(x, y);
            
            g.setColor(new Color(0, 0, 0, 55 + 200*(Math.abs(time - 40))/40));
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
