package battle.effect.generic;

import battle.effect.generic.WeatherEffect.ClearSkies;
import battle.effect.generic.WeatherEffect.Hailing;
import battle.effect.generic.WeatherEffect.Raining;
import battle.effect.generic.WeatherEffect.Sandstorm;
import battle.effect.generic.WeatherEffect.Sunny;

import java.util.function.Supplier;

public enum WeatherNamesies implements EffectNamesies2 {
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

