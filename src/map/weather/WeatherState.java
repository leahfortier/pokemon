package map.weather;

import battle.effect.generic.EffectNamesies;
import draw.DrawUtils;

import java.awt.Color;
import java.awt.Graphics;

public enum WeatherState {
    NORMAL(EffectNamesies.CLEAR_SKIES),
    SUN(EffectNamesies.SUNNY, g -> DrawUtils.fillCanvas(g, new Color(255, 255, 255, 64))),
    RAIN(EffectNamesies.RAINING, new RainyState()),
    FOG(EffectNamesies.CLEAR_SKIES),    // TODO: These
    SNOW(EffectNamesies.HAILING);

    private final EffectNamesies weatherEffect;
    private final WeatherDrawer weatherDrawer;

    WeatherState(EffectNamesies weatherEffect) {
        this(weatherEffect, null);
    }

    WeatherState(EffectNamesies weatherEffect, WeatherDrawer weatherDrawer) {
        this.weatherEffect = weatherEffect;
        this.weatherDrawer = weatherDrawer;
    }

    public EffectNamesies getWeatherEffect() {
        return this.weatherEffect;
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
