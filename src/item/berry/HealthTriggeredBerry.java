package item.berry;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectInterfaces.DamageTakenEffect;
import pokemon.ability.AbilityNamesies;

public interface HealthTriggeredBerry extends GainableEffectBerry, DamageTakenEffect {
    double healthTriggerRatio();

    @Override
    default void damageTaken(Battle b, ActivePokemon damageTaker) {
        double healthRatio = damageTaker.getHPRatio();
        if ((healthRatio <= this.healthTriggerRatio() || (healthRatio <= .5 && damageTaker.hasAbility(AbilityNamesies.GLUTTONY)))) {
            if (this.gainBerryEffect(b, damageTaker, CastSource.HELD_ITEM)) {
                damageTaker.consumeItem(b);
            }
        }
    }
}
