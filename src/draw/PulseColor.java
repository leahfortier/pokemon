package draw;

import java.awt.Color;
import java.awt.Graphics;

public class PulseColor {
    private final Color baseColor;

    private int time = 0;

    public PulseColor(Color baseColor) {
        this.baseColor = baseColor;
    }

    public void setColor(Graphics g) {
        time = (time + 1)%80;
        g.setColor(new Color(
                baseColor.getRed(),
                baseColor.getGreen(),
                baseColor.getBlue(),
                55 + 150*(Math.abs(time - 40))/40)
        );
    }
}
