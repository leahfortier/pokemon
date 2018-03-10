package battle.effect.battle.weather;

import battle.effect.EffectNamesies.BattleEffectNamesies;
import battle.effect.battle.weather.WeatherEffect.ClearSkies;
import battle.effect.battle.weather.WeatherEffect.Hailing;
import battle.effect.battle.weather.WeatherEffect.Raining;
import battle.effect.battle.weather.WeatherEffect.Sandstorm;
import battle.effect.battle.weather.WeatherEffect.Sunny;

import java.util.function.Supplier;

public enum WeatherNamesies implements BattleEffectNamesies {
    // EVERYTHING BELOW IS GENERATED ###
    CLEAR_SKIES(ClearSkies::new),
    RAINING(Raining::new),
    SUNNY(Sunny::new),
    SANDSTORM(Sandstorm::new),
    HAILING(Hailing::new);

    // EVERYTHING ABOVE IS GENERATED ###

    private final Supplier<WeatherEffect> effectCreator;

    WeatherNamesies(Supplier<WeatherEffect> effectCreator) {
        this.effectCreator = effectCreator;
    }

    @Override
    public WeatherEffect getEffect() {
        return this.effectCreator.get();
    }
}

