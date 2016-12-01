package map.weather;

import java.awt.Graphics;

public enum WeatherState {
    NORMAL,
    SUN(new SunnyState()),
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
}
