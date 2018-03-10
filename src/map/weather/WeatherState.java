package map.weather;

import battle.effect.generic.battle.weather.WeatherNamesies;
import draw.DrawUtils;

import java.awt.Color;
import java.awt.Graphics;

public enum WeatherState {
    NORMAL(WeatherNamesies.CLEAR_SKIES),
    SUN(WeatherNamesies.SUNNY, g -> DrawUtils.fillCanvas(g, new Color(255, 255, 255, 64))),
    RAIN(WeatherNamesies.RAINING, new RainyState()),
    SANDSTORM(WeatherNamesies.SANDSTORM), // TODO: These
    SNOW(WeatherNamesies.HAILING);

    private final WeatherNamesies weatherEffect;
    private final WeatherDrawer weatherDrawer;

    WeatherState(WeatherNamesies weatherEffect) {
        this(weatherEffect, null);
    }

    WeatherState(WeatherNamesies weatherEffect, WeatherDrawer weatherDrawer) {
        this.weatherEffect = weatherEffect;
        this.weatherDrawer = weatherDrawer;
    }

    public WeatherNamesies getWeatherEffect() {
        return this.weatherEffect;
    }

    public void draw(Graphics g) {
        if (this.weatherDrawer != null) {
            this.weatherDrawer.draw(g);
        }
    }

    @FunctionalInterface
    interface WeatherDrawer {
        void draw(Graphics g);
    }
}
