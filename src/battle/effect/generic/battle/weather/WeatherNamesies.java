package battle.effect.generic.battle.weather;

import battle.effect.generic.battle.weather.WeatherEffect.ClearSkies;
import battle.effect.generic.battle.weather.WeatherEffect.Hailing;
import battle.effect.generic.battle.weather.WeatherEffect.Raining;
import battle.effect.generic.battle.weather.WeatherEffect.Sandstorm;
import battle.effect.generic.battle.weather.WeatherEffect.Sunny;
import battle.effect.generic.EffectNamesies.BattleEffectNamesies;

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

