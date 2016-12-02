package map.weather;

import util.DrawUtils;

import java.awt.Color;
import java.awt.Graphics;

public enum WeatherState {
    NORMAL,
    SUN(g -> DrawUtils.fillCanvas(g, new Color(255, 255, 255, 64))),
    RAIN(new RainyState()),
    FOG,    // TODO: These
    SNOW;

    private final WeatherDrawer weatherDrawer;

    WeatherState() {
        this(null);
    }

    WeatherState(WeatherDrawer weatherDrawer) {
        this.weatherDrawer = weatherDrawer;
    }

    public void draw(Graphics g) {
        if (this.weatherDrawer != null) {
            this.weatherDrawer.draw(g);
        }
    }

    interface WeatherDrawer {
        void draw(Graphics g);
    }
}
